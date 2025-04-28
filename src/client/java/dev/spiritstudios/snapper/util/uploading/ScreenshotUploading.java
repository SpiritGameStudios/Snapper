package dev.spiritstudios.snapper.util.uploading;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import dev.spiritstudios.snapper.Snapper;
import dev.spiritstudios.snapper.SnapperConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;

public class ScreenshotUploading {
	public static final String SNAPPER_WEB_URL = "https://api.axolotlclient.com/v1/image/%s/view";
	public static final boolean AXOLOTLCLIENT_LOADED = FabricLoader.getInstance().isModLoaded("axolotlclient");
	public static final String SNAPPER_VERSION = FabricLoader.getInstance().getModContainer("snapper").orElseThrow().getMetadata().getVersion().getFriendlyString();
	private static final ScreenshotUploading INSTANCE = new ScreenshotUploading();

	public static ScreenshotUploading getInstance() {
		return INSTANCE;
	}

	private final AxolotlClientApi api = new AxolotlClientApi();

	private boolean isOfflineAccount() {
		return MinecraftClient.getInstance().getSession().getAccessToken().length() < 400;
	}

	private void toast(String title, String description, Object... args) {
		MinecraftClient.getInstance().getToastManager().add(
				SystemToast.create(MinecraftClient.getInstance(),
						SystemToast.Type.WORLD_BACKUP,
						Text.translatable(title, args),
						Text.translatable(description, args)));
	}

	public CompletableFuture<?> upload(Path image) {
		if (AXOLOTLCLIENT_LOADED) {
			if (!AxolotlClientCompat.isApiOnline()) {
				toast("toast.snapper.upload.failure", "toast.snapper.upload.axolotlclient.api_disabled");
				Snapper.LOGGER.info("API is disabled in AxolotlClient's Settings!");
				return CompletableFuture.completedFuture(null);
			}
			return AxolotlClientCompat.upload(image).thenAccept(this::imageUploaded);
		}

		if (isOfflineAccount()) {
			return CompletableFuture.completedFuture(null);
		}

		if (SnapperConfig.INSTANCE.termsAccepted.get() == AxolotlClientApi.TermsAcceptance.UNSET) {
			var client = MinecraftClient.getInstance();
			var cf = new CompletableFuture<>();
			client.setScreen(new PrivacyNoticeScreen(client.currentScreen, v -> {
				if (v) {
					upload(image).thenAccept(cf::complete);
				}
			}));
			return cf;
		}

		if (SnapperConfig.INSTANCE.termsAccepted.get() != AxolotlClientApi.TermsAcceptance.ACCEPTED) {
			toast("toast.snapper.upload.failure", "toast.snapper.upload.axolotlclient.api_disabled");
			return CompletableFuture.completedFuture(null);
		}

		return api.uploadImage(image).thenAccept(this::imageUploaded);
	}

	private void imageUploaded(String imageId) {
		if (imageId == null) {
			toast("toast.snapper.upload.failure", "toast.snapper.upload.failure.generic");
			return;
		}
		var client = MinecraftClient.getInstance();
		String snapperUrl = SNAPPER_WEB_URL.formatted(imageId);
		Snapper.LOGGER.info("Uploaded screenshot to: {}", snapperUrl);
		client.keyboard.setClipboard(snapperUrl);
		toast("toast.snapper.upload.success", "toast.snapper.upload.success.description", snapperUrl);
	}
}
