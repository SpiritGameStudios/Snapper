package dev.spiritstudios.snapper.util.uploading;

import java.nio.file.Path;
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
			var upload = imgShareCls.getMethod("upload");
			upload.setAccessible(true);
			return (CompletableFuture<String>) upload.invoke(networking, image);
		} catch (Exception e) {
			return CompletableFuture.failedFuture(e);
		}
	}
}
