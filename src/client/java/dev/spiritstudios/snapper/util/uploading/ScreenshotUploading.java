package dev.spiritstudios.snapper.util.uploading;

import dev.spiritstudios.snapper.Snapper;
import dev.spiritstudios.snapper.SnapperConfig;
import dev.spiritstudios.snapper.gui.screen.PrivacyNoticeScreen;
import dev.spiritstudios.snapper.gui.toast.SnapperToast;
import dev.spiritstudios.snapper.util.SnapperUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class ScreenshotUploading {
    public static final String SNAPPER_WEB_URL = "https://snapper.spiritstudios.dev/img/%s";
    public static final String SNAPPER_VERSION = FabricLoader.getInstance().getModContainer("snapper")
            .orElseThrow()
            .getMetadata().getVersion().getFriendlyString();

    private static final AxolotlClientApi API = new AxolotlClientApi();

    public static CompletableFuture<Void> upload(Path image) {
        if (SnapperUtil.isOfflineAccount()) {
            SnapperUtil.toast(
                    SnapperToast.Type.DENY,
                    Text.translatable("toast.snapper.upload.axolotlclient.api_disabled"),
                    Text.translatable("toast.snapper.upload.offline")
            );
            return CompletableFuture.failedFuture(new IllegalStateException("Minecraft is currently running in offline mode."));
        }

        if (SnapperConfig.INSTANCE.termsAccepted.get() == AxolotlClientApi.TermsAcceptance.UNSET) {
            MinecraftClient client = MinecraftClient.getInstance();
            CompletableFuture<Void> success = new CompletableFuture<>();

            client.setScreen(new PrivacyNoticeScreen(client.currentScreen, v -> {
                if (v) upload(image).thenAccept(success::complete);
            }));

            return success;
        }

        if (SnapperConfig.INSTANCE.termsAccepted.get() != AxolotlClientApi.TermsAcceptance.ACCEPTED) {
            SnapperUtil.toast(
                    SnapperToast.Type.DENY,
                    Text.translatable("toast.snapper.upload.failure"),
                    Text.translatable("toast.snapper.upload.axolotlclient.api_disabled")
            );
            return CompletableFuture.failedFuture(new IllegalStateException("AxolotlClient API is disabled."));
        }

        return API
                .uploadImage(image)
                .thenAccept(ScreenshotUploading::imageUploaded);
    }

    private static void imageUploaded(String imageId) {
        if (imageId == null) {
            SnapperUtil.toast(
                    SnapperToast.Type.DENY,
                    Text.translatable("toast.snapper.upload.failure"),
                    Text.translatable("toast.snapper.upload.failure.generic")
            );
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        String snapperUrl = SNAPPER_WEB_URL.formatted(imageId);

        Snapper.LOGGER.info("Uploaded screenshot to: {}", snapperUrl);

        client.keyboard.setClipboard(snapperUrl);
        SnapperUtil.toast(
                SnapperToast.Type.UPLOAD,
                Text.translatable("toast.snapper.upload.success"),
                Text.translatable("toast.snapper.upload.success.description", snapperUrl)
        );
    }

    public static void close() {
        API.close();
    }
}
