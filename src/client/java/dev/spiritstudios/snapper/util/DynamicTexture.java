package dev.spiritstudios.snapper.util;

import dev.spiritstudios.snapper.Snapper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class DynamicTexture implements AutoCloseable {
    private static final Identifier UNKNOWN_SERVER = Identifier.ofVanilla("textures/misc/unknown_server.png");

    private final TextureManager textureManager;
    private final Identifier id;
    private final Path path;

    private final NativeImage image;
    private NativeImageBackedTexture texture;

    private DynamicTexture(TextureManager textureManager, Identifier id, Path path) throws IOException {
        this.textureManager = textureManager;
        this.id = id;

        this.path = path;

        try (InputStream stream = Files.newInputStream(path)) {
            this.image = NativeImage.read(stream);
        }
    }

    public CompletableFuture<Void> load() {
        return MinecraftClient.getInstance().submit(() -> {
            this.texture = new NativeImageBackedTexture(this.id::toString, this.image);
            this.textureManager.registerTexture(this.id, this.texture);
        });
    }

    public static Optional<DynamicTexture> createScreenshot(TextureManager textureManager, Path path) {
        try {
            return Optional.of(new DynamicTexture(
                    textureManager,
					Snapper.id(
							"screenshots/" + Util.replaceInvalidChars(path.getFileName().toString(), Identifier::isPathCharacterValid) + "/icon"
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
        this.textureManager.destroyTexture(this.id);
        this.texture.close();
    }

    public int getWidth() {
        return this.texture != null && this.texture.getImage() != null ? this.texture.getImage().getWidth() : 64;
    }

    public int getHeight() {
        return this.texture != null && this.texture.getImage() != null ? this.texture.getImage().getHeight() : 64;
    }

    public Identifier getTextureId() {
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
