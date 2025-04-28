package dev.spiritstudios.snapper.util.uploading;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import dev.spiritstudios.snapper.Snapper;
import io.github.axolotlclient.api.API;
import io.github.axolotlclient.api.Options;
import io.github.axolotlclient.api.util.UUIDHelper;
import io.github.axolotlclient.modules.auth.Account;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;

public class ScreenshotUploading {
	public static final String SNAPPER_WEB_URL = "https://snapper.spiritstudios.dev/img/%s";
	public static final boolean AXOLOTLCLIENT_LOADED = FabricLoader.getInstance().isModLoaded("axolotlclient");
	public static final String SNAPPER_VERSION = FabricLoader.getInstance().getModContainer("snapper").orElseThrow().getMetadata().getVersion().getFriendlyString();
	private static final ScreenshotUploading INSTANCE = new ScreenshotUploading();

	public static ScreenshotUploading getInstance() {
		return INSTANCE;
	}

	private final ImageNetworkingImpl networking = new ImageNetworkingImpl();

	private Account getCurrentAccount() {
		var client = MinecraftClient.getInstance();
		return new Account(client.getSession().getUsername(), UUIDHelper.toUndashed(client.getSession().getUuidOrNull()), client.getSession().getAccessToken());
	}

	public CompletableFuture<?> upload(Path image) {
		ApiSupplier.loadAPI();

		if (AXOLOTLCLIENT_LOADED) {
			if (!API.getInstance().isAuthenticated()) {
				API.getInstance().getNotificationProvider().addStatus("gallery.image.upload.failure", "toast.snapper.upload.axolotlclient.api_disabled");
				Snapper.LOGGER.info("API is disabled in AxolotlClient's Settings!");
				return CompletableFuture.completedFuture(null);
			}
			return networking.upload(image).thenAccept(this::imageUploaded);
		}


		var account = getCurrentAccount();
		if (account.isOffline()) {
			return CompletableFuture.completedFuture(null);
		}

		API.getInstance().startup(account);
		return CompletableFuture.supplyAsync(() -> {
			// The author of the API (me) clearly has done too well at protecting its internals
			// Therefore we may pick our poison here: either a busy-wait loop or a queue, the latter being
			// more difficult as we don't really know when to remove things from the queue again
			while (!API.getInstance().isAuthenticated()) {
				try {
					//noinspection BusyWait
					Thread.sleep(100);
				} catch (InterruptedException ignored) {
				}
				if (API.getInstance().getApiOptions().privacyAccepted.get() == Options.PrivacyPolicyState.DENIED) {
					API.getInstance().getNotificationProvider().addStatus("gallery.image.upload.failure", "toast.snapper.upload.axolotlclient.api_disabled");
					return null;
				}
			}
			return networking.upload(image).thenAccept(this::imageUploaded).thenRun(API.getInstance()::shutdown);
		});
	}

	private void imageUploaded(String axolotlClientUrl) {
		var client = MinecraftClient.getInstance();
		String id = networking.getIdFromUrl(axolotlClientUrl);
		String snapperUrl = SNAPPER_WEB_URL.formatted(id);
		Snapper.LOGGER.info("Uploaded screenshot to: {}", snapperUrl);
		client.keyboard.setClipboard(snapperUrl);
		API.getInstance().getNotificationProvider().addStatus("gallery.image.upload.success", "gallery.image.upload.success.description", snapperUrl);
	}
}
