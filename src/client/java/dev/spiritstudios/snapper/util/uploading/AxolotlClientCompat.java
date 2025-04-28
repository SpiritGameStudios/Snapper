package dev.spiritstudios.snapper.util.uploading;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import io.github.axolotlclient.api.API;
import io.github.axolotlclient.modules.screenshotUtils.ImageNetworking;

public class AxolotlClientCompat {

	public static boolean isApiOnline() {
		return API.getInstance().isAuthenticated();
	}

	@SuppressWarnings("unchecked")
	public static CompletableFuture<String> upload(Path image) {
		try {
			var imgShareCls = Class.forName("io.github.axolotlclient.modules.screenshotUtils.ImageShare");
			ImageNetworking networking = (ImageNetworking) imgShareCls.getMethod("getInstance").invoke(null);
			var imgNetworkCls = Class.forName("io.github.axolotlclient.modules.screenshotUtils.ImageNetworking");
			var upload = imgNetworkCls.getDeclaredMethod("upload", Path.class);
			upload.setAccessible(true);
			var urlToId = imgNetworkCls.getDeclaredMethod("urlToId", String.class);
			urlToId.setAccessible(true);
			return ((CompletableFuture<String>) upload.invoke(networking, image)).thenApply(s -> {
				try {
					return ((Optional<String>) urlToId.invoke(networking, s)).orElseThrow();
				} catch (Exception e) {
					return null;
				}
			});
		} catch (Exception e) {
			return CompletableFuture.completedFuture(null);
		}
	}
}
