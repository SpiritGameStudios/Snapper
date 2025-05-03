package dev.spiritstudios.snapper.util.uploading;

import dev.spiritstudios.snapper.Snapper;
import dev.spiritstudios.snapper.SnapperConfig;
import dev.spiritstudios.snapper.gui.screen.PrivacyNoticeScreen;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class ScreenshotUploading {
	public static final String SNAPPER_WEB_URL = "https://snapper.spiritstudios.dev/img/%s";
	public static final String SNAPPER_VERSION = FabricLoader.getInstance().getModContainer("snapper")
			.orElseThrow()
			.getMetadata().getVersion().getFriendlyString();

	private static final AxolotlClientApi API = new AxolotlClientApi();

	private static boolean isOfflineAccount() {
		return MinecraftClient.getInstance().getSession().getAccessToken().length() < 400;
	}

	public static void toast(String title, String description, Object... args) {
		MinecraftClient.getInstance().getToastManager().add(
				SystemToast.create(MinecraftClient.getInstance(),
						SystemToast.Type.WORLD_BACKUP,
						Text.translatable(title, args),
						Text.translatable(description, args)));
	}

	public static CompletableFuture<Void> upload(Path image) {
		if (isOfflineAccount()) {
			toast("toast.snapper.upload.failure", "toast.snapper.upload.offline");
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
			toast("toast.snapper.upload.failure", "toast.snapper.upload.axolotlclient.api_disabled");
			return CompletableFuture.failedFuture(new IllegalStateException("AxolotlClient API is disabled."));
		}

		return API
				.uploadImage(image)
				.thenAccept(ScreenshotUploading::imageUploaded);
	}

	private static void imageUploaded(String imageId) {
		if (imageId == null) {
			toast("toast.snapper.upload.failure", "toast.snapper.upload.failure.generic");
			return;
		}

        MinecraftClient client = MinecraftClient.getInstance();
		String snapperUrl = SNAPPER_WEB_URL.formatted(imageId);

		Snapper.LOGGER.info("Uploaded screenshot to: {}", snapperUrl);

		client.keyboard.setClipboard(snapperUrl);
		toast("toast.snapper.upload.success", "toast.snapper.upload.success.description", snapperUrl);
	}

	public static void close() {
		API.close();
	}
}
