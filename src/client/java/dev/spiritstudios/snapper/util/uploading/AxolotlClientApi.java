package dev.spiritstudios.snapper.util.uploading;

import java.io.IOException;
import java.math.BigInteger;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.util.UndashedUuid;
import dev.spiritstudios.snapper.SnapperConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;
import net.minecraft.util.Util;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AxolotlClientApi {

	public enum TermsAcceptance {
		ACCEPTED,
		DENIED,
		UNSET
	}

	private final Logger logger = LoggerFactory.getLogger("AxolotlClient/API (Snapper)");
	private HttpClient client;
	private String token;

	public CompletableFuture<String> run(Path image) {
		try {
			if (get("global_data", Map.of()).get(1L, TimeUnit.MINUTES)
					.code != 200) {
				this.logger.warn("Not trying to start API as it couldn't be reached!");
				return CompletableFuture.completedFuture(null);
			}
		} catch (ExecutionException | TimeoutException | InterruptedException var2) {
			this.logger.warn("Not trying to start API as it couldn't be reached within the timeout of 1 minute!");
			return CompletableFuture.completedFuture(null);
		}
		byte[] bytes;
		try {
			bytes = Files.readAllBytes(image);
		} catch (IOException e) {
			return CompletableFuture.completedFuture(null);
		}

		var minecraftSession = MinecraftClient.getInstance().getSession();
		String serverId = authenticateMojang(minecraftSession);
		if (serverId == null) {
			this.logger.error("Failed to authenticate with Mojang!");
		} else {
			return CompletableFuture.supplyAsync(() -> {
				this.get("authenticate", Map.of("username", minecraftSession.getUsername(), "server_id", serverId)).whenComplete((response, throwable) -> {
					if (throwable != null) {
						this.logger.error("Failed to authenticate!", throwable);
					} else if (response.code >= 300 || response.code < 200) {
						this.logger.error("Failed to authenticate!");
					} else {
						this.token = new Gson().fromJson(response.body, JsonObject.class).get("access_token").getAsString();
					}
				}).join();
				String id = post("image/" + image.getFileName().toString(), bytes).thenApply((r) -> {
					if (r.code != 200) {
						return null;
					}
					return r.body;
				}).join();
				token = null;
				return id;
			});
		}
		return CompletableFuture.completedFuture(null);
	}

	public CompletableFuture<Response> get(String route, Map<String, String> query) {
		return this.request(route, query, null, "GET");
	}

	public CompletableFuture<Response> post(String route, byte[] rawBody) {
		return this.request(route, null, rawBody, "POST");
	}

	private CompletableFuture<Response> request(String route, Map<String, String> query, byte[] rawBody, String method) {
		if (SnapperConfig.INSTANCE.termsAccepted.get() != TermsAcceptance.ACCEPTED) {
			return CompletableFuture.completedFuture(Response.CLIENT_ERROR);
		} else {
			StringBuilder url = new StringBuilder("https://api.axolotlclient.com/v1/");
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
	}

	public static String authenticateMojang(Session account) {
		try (HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build()) {
			HttpRequest.Builder builder = HttpRequest.newBuilder().timeout(Duration.ofSeconds(10L));
			builder.header("Content-Type", "application/json; charset=utf-8");
			builder.header("Accept", "application/json");
			builder.header("user-agent", "Snapper/" + ScreenshotUploading.SNAPPER_VERSION + " MojangAuth");
			JsonObject body = new JsonObject();
			body.addProperty("accessToken", account.getAccessToken());
			body.addProperty("selectedProfile", UndashedUuid.toString(account.getUuidOrNull()));
			String serverId = minecraftSha1(RandomStringUtils.random(40).getBytes(StandardCharsets.UTF_8));
			body.addProperty("serverId", serverId);
			builder.header("Authorization", "Bearer " + account.getAccessToken());
			builder.POST(HttpRequest.BodyPublishers.ofByteArray(body.toString().getBytes(StandardCharsets.UTF_8)));
			builder.uri(URI.create("https://sessionserver.mojang.com/session/minecraft/join"));
			HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
			if ((response.body() == null || response.body().isEmpty()) && response.statusCode() == 204) {
				return serverId;
			}
			return null;
		} catch (Exception ignored) {
		}
		return null;
	}

	private static String minecraftSha1(byte[]... bytes) {
		int length = Arrays.stream(bytes).mapToInt((a) -> a.length).sum();
		byte[] data = new byte[length];
		int index = 0;

		for (byte[] arr : bytes) {
			int size = arr.length;
			System.arraycopy(arr, 0, data, index, size);
			index += size;
		}

		try {
			return (new BigInteger(MessageDigest.getInstance("SHA-1").digest(data))).toString(16);
		} catch (NoSuchAlgorithmException var9) {
			return null;
		}
	}

	private CompletableFuture<Response> request(URI url, byte[] rawBody, String method) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				HttpRequest.Builder builder = HttpRequest.newBuilder(url).header("Content-Type", "application/json").header("Accept", "application/json");
				if (this.token != null) {
					builder.header("Authorization", this.token);
				}

				if (rawBody != null) {
					builder.method(method, HttpRequest.BodyPublishers.ofByteArray(rawBody));
				} else {
					builder.method(method, HttpRequest.BodyPublishers.noBody());
				}

				builder.header("user-agent", "Snapper/" + ScreenshotUploading.SNAPPER_VERSION);

				if (this.client == null) {
					this.client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
				}

				HttpResponse<String> response = this.client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
				String body = response.body();
				int code = response.statusCode();

				return new Response(body, code);
			} catch (HttpTimeoutException | ConnectException var10) {
				this.logger.warn("Backend unreachable!");
				return Response.CLIENT_ERROR;
			} catch (Exception e) {
				return Response.CLIENT_ERROR;
			}
		}, Util.getIoWorkerExecutor());
	}

	public record Response(String body, int code) {
		public static final Response CLIENT_ERROR = new Response("", 400);
	}
}
