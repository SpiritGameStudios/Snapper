package dev.spiritstudios.snapper.util.uploading;

import com.google.gson.JsonParser;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.serialization.JsonOps;
import dev.spiritstudios.snapper.Snapper;
import dev.spiritstudios.snapper.SnapperConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;
import net.minecraft.util.Util;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class AxolotlClientApi implements Closeable {
    public enum TermsAcceptance {
        ACCEPTED,
        DENIED,
        UNSET
    }

    private static final String BASE_URL = "https://api.axolotlclient.com/v1/";

    private final HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .executor(Util.getIoWorkerExecutor())
            .build();

    private Instant authTime = Instant.EPOCH;
    private @Nullable AxolotlAuthentication auth;

    public CompletableFuture<String> uploadImage(Path image) {
        byte[] bytes;
        try {
            bytes = Files.readAllBytes(image);
        } catch (IOException e) {
            return CompletableFuture.failedFuture(e);
        }

        ScreenshotUploading.toast("toast.snapper.upload.in_progress", "toast.snapper.upload.in_progress.description");

        return authenticate()
                .thenCompose(ignored -> post("image/" + image.getFileName().toString(), bytes))
                .thenApply(response -> {
                    if (response.statusCode() != 200) return null;
                    return response.body();
                })
                .exceptionally(e -> {
                    Snapper.LOGGER.error("Failed to upload image", e);
                    return null;
                });
    }

    private CompletableFuture<Void> authenticate() {
        if (authTime.plus(24, ChronoUnit.HOURS).isAfter(Instant.now()))
            return CompletableFuture.completedFuture(null);

        Session session = MinecraftClient.getInstance().getSession();
        MinecraftSessionService sessionService = MinecraftClient.getInstance().getSessionService();
        String serverId = new BigInteger(DigestUtils.sha1(RandomStringUtils.insecure().next(40).getBytes(StandardCharsets.UTF_8))).toString(16);

        try {
            sessionService.joinServer(session.getUuidOrNull(), session.getAccessToken(), serverId);
        } catch (AuthenticationException e) {
            return CompletableFuture.failedFuture(e);
        }

        return this.get("authenticate", Map.of("username", session.getUsername(), "server_id", serverId))
                .thenApply(response ->
                        AxolotlAuthentication.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(response.body()))
                                .getOrThrow())
                .thenAccept(auth -> {
                    this.auth = auth;
                    this.authTime = Instant.now();
                });
    }

    public CompletableFuture<HttpResponse<String>> get(String route, Map<String, String> query) {
        return this.request(route, query, null, "GET");
    }

    public CompletableFuture<HttpResponse<String>> post(String route, byte[] rawBody) {
        return this.request(route, null, rawBody, "POST");
    }

    private CompletableFuture<HttpResponse<String>> request(String route, Map<String, String> query, byte[] rawBody, String method) {
        if (SnapperConfig.termsAccepted != TermsAcceptance.ACCEPTED)
            return CompletableFuture.failedFuture(new IllegalStateException("Terms not accepted"));

        StringBuilder url = new StringBuilder(BASE_URL);
        url.append(route);

        if (query != null && !query.isEmpty()) {
            url.append("?");
            query.forEach((k, v) -> {
                if (url.charAt(url.length() - 1) != '?') {
                    url.append("&");
                }

                url.append(k).append("=").append(v);
            });
        }

        return this.request(URI.create(url.toString()), rawBody, method);
    }

    private CompletableFuture<HttpResponse<String>> request(URI url, byte[] rawBody, String method) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(url)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("User-Agent", "Snapper/" + ScreenshotUploading.SNAPPER_VERSION);

        if (auth != null) builder.header("Authorization", this.auth.accessToken());


        builder.method(
                method,
                rawBody != null ?
                        HttpRequest.BodyPublishers.ofByteArray(rawBody) :
                        HttpRequest.BodyPublishers.noBody()
        );

        return this.client.sendAsync(builder.build(), HttpResponse.BodyHandlers.ofString());

    }

    @Override
    public void close() {
        client.close();
    }
}
