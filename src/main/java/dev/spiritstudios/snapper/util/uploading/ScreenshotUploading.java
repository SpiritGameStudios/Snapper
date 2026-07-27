package dev.spiritstudios.snapper.util.uploading;

import dev.spiritstudios.snapper.Snapper;
import dev.spiritstudios.snapper.SnapperConfig;
import dev.spiritstudios.snapper.gui.screen.PrivacyNoticeScreen;
import dev.spiritstudios.snapper.gui.toast.SnapperToast;
import dev.spiritstudios.snapper.gui.toast.SnapperToasts;
import dev.spiritstudios.snapper.util.SnapperUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class ScreenshotUploading {
    public static final String SNAPPER_WEB_URL = "https://snapper.spiritstudios.dev/img/%s";
    public static final String SNAPPER_VERSION = FabricLoader.getInstance().getModContainer(Snapper.MOD_ID)
            .orElseThrow()
            .getMetadata().getVersion().getFriendlyString();

    private static final AxolotlClientApi API = new AxolotlClientApi();

    private static CompletableFuture<Void> requestTerms() {
        Minecraft minecraft = Minecraft.getInstance();
        CompletableFuture<Void> done = new CompletableFuture<>();

        minecraft.gui.setScreen(new PrivacyNoticeScreen(minecraft.gui.screen(), () -> {
            done.complete(null);
        }));

        return done;
    }

    public static CompletableFuture<Void> upload(Path image, boolean promptAcceptTerms) {
        if (SnapperUtil.isOfflineAccount()) {
            SnapperToasts.imageUploadFailureOffline();
            return CompletableFuture.failedFuture(new IllegalStateException("Minecraft is currently running in offline mode."));
        }

        AxolotlClientApi.TermsAcceptance termsStatus = SnapperConfig.get().axolotlClient().termsStatus();

        if (termsStatus == AxolotlClientApi.TermsAcceptance.DENIED) {
            SnapperToasts.imageUploadApiDisabledToast();
            return CompletableFuture.failedFuture(new IllegalStateException("AxolotlClient API is disabled."));
        }

        if (termsStatus == AxolotlClientApi.TermsAcceptance.UNSET) {
            if (promptAcceptTerms) {
                return requestTerms().thenCompose(_ -> upload(image, false));
            } else {
                SnapperToasts.imageUploadApiDisabledToast();
                return CompletableFuture.failedFuture(new IllegalStateException("AxolotlClient API was not accepted."));
            }
        }

        return API
                .uploadImage(image)
                .thenAccept(ScreenshotUploading::imageUploaded);
    }

    private static void imageUploaded(String imageId) {
        if (imageId == null) {
            SnapperToasts.imageUploadFailureGeneric();
            return;
        }

        Minecraft client = Minecraft.getInstance();
        String snapperUrl = SNAPPER_WEB_URL.formatted(imageId);

        client.keyboardHandler.setClipboard(snapperUrl);
        SnapperToasts.imageUploadSuccess(Component.literal(snapperUrl));
    }

    public static void close() {
        API.close();
    }
}
