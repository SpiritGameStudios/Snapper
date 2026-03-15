package dev.spiritstudios.snapper.util;

import com.mojang.blaze3d.platform.NativeImage;
import dev.spiritstudios.snapper.Snapper;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ScreenshotTexture implements AutoCloseable {
    private static final Identifier UNKNOWN_SERVER = Identifier.withDefaultNamespace("textures/misc/unknown_server.png");

    private final TextureManager textureManager;
    private final Identifier textureLocation;
    private final Path path;

    private final NativeImage image;
    private DynamicTexture texture;

    private ScreenshotTexture(TextureManager textureManager, Identifier textureLocation, Path path) throws IOException {
        this.textureManager = textureManager;
        this.textureLocation = textureLocation;

        this.path = path;

        try (InputStream stream = Files.newInputStream(path)) {
            this.image = NativeImage.read(stream);
        }
    }

    public CompletableFuture<Void> load() {
        return Minecraft.getInstance().submit(() -> {
            this.texture = new DynamicTexture(this.textureLocation::toString, this.image);
            this.textureManager.register(this.textureLocation, this.texture);
        });
    }

    public static Optional<ScreenshotTexture> createScreenshot(TextureManager textureManager, Path path) {
        try {
            return Optional.of(new ScreenshotTexture(
                    textureManager,
					Snapper.id(
							"screenshots/" + Util.sanitizeName(path.getFileName().toString(), Identifier::validPathChar) + "/icon"
					),
                    path
            ));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public void destroy() {
        this.textureManager.release(this.textureLocation);
        this.texture.close();
    }

    public int getWidth() {
        return this.texture != null ? this.texture.getTexture().getWidth(0) : 64;
    }

    public int getHeight() {
        return this.texture != null ? this.texture.getTexture().getHeight(0) : 64;
    }

    public Identifier textureLocation() {
        return this.texture != null ? this.textureLocation : UNKNOWN_SERVER;
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
