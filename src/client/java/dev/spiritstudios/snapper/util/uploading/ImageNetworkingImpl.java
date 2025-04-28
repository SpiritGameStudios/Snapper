package dev.spiritstudios.snapper.util.uploading;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import io.github.axolotlclient.modules.screenshotUtils.ImageNetworking;

public class ImageNetworkingImpl extends ImageNetworking {
	@Override
	public void uploadImage(Path path) {

	}

	@Override
	public CompletableFuture<String> upload(Path file) {
		return super.upload(file);
	}

	public String getIdFromUrl(String url) {
		return urlToId(url).orElseThrow();
	}
}
