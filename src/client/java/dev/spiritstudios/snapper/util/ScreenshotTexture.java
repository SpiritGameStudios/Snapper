package dev.spiritstudios.snapper.util;

import com.mojang.blaze3d.platform.NativeImage;
import dev.spiritstudios.snapper.Snapper;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ScreenshotTexture implements AutoCloseable {
    private static final ResourceLocation UNKNOWN_SERVER = ResourceLocation.withDefaultNamespace("textures/misc/unknown_server.png");

    private final TextureManager textureManager;
    private final ResourceLocation id;
    private final Path path;

    private final NativeImage image;
    private DynamicTexture texture;

    private ScreenshotTexture(TextureManager textureManager, ResourceLocation id, Path path) throws IOException {
        this.textureManager = textureManager;
        this.id = id;

        this.path = path;

        try (InputStream stream = Files.newInputStream(path)) {
            this.image = NativeImage.read(stream);
        }
    }

    public CompletableFuture<Void> load() {
        return Minecraft.getInstance().submit(() -> {
            this.texture = new DynamicTexture(this.id::toString, this.image);
            this.textureManager.register(this.id, this.texture);
        });
    }

    public static Optional<ScreenshotTexture> createScreenshot(TextureManager textureManager, Path path) {
        try {
            return Optional.of(new ScreenshotTexture(
                    textureManager,
					Snapper.id(
							"screenshots/" + Util.sanitizeName(path.getFileName().toString(), ResourceLocation::validPathChar) + "/icon"
					),
                    path
            ));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    /*
     * Must be called on render thread
     */
    public void enableFiltering() {
        this.texture.setFilter(true, true);
    }

    public void destroy() {
        this.textureManager.release(this.id);
        this.texture.close();
    }

    public int getWidth() {
        return this.texture != null ? this.texture.getTexture().getWidth(0) : 64;
    }

    public int getHeight() {
        return this.texture != null ? this.texture.getTexture().getHeight(0) : 64;
    }

    public ResourceLocation getTextureId() {
        return this.texture != null ? this.id : UNKNOWN_SERVER;
    }

    public boolean loaded() {
        return texture != null;
    }

    public Path getPath() {
        return path;
    }

    public void close() {
        this.destroy();
    }
}
