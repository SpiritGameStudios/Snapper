package dev.spiritstudios.snapper.util;

import com.google.common.hash.Hashing;
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
    public final Path path;

    private boolean loaded;
    private DynamicTexture texture;
    private boolean closed;

    private ScreenshotTexture(TextureManager textureManager, Identifier textureLocation, Path path) throws IOException {
        this.textureManager = textureManager;
        this.textureLocation = textureLocation;

        this.path = path;
    }

    @SuppressWarnings("deprecation") // Vanilla uses sha1 for this, copying it
    public static Optional<ScreenshotTexture> createScreenshot(TextureManager textureManager, Path path) {
        String name = path.getFileName().toString();

        try {
            return Optional.of(new ScreenshotTexture(
                    textureManager,
                    Snapper.id(
                            "screenshots/" + Util.sanitizeName(name, Identifier::validPathChar) + "/" + Hashing.sha1().hashUnencodedChars(name)
                    ),
                    path
            ));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public NativeImage load() {
        try (InputStream stream = Files.newInputStream(path)) {
            return NativeImage.read(stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void startLoading(Minecraft minecraft) {
        if (!isLoaded()) {
            CompletableFuture
                    .supplyAsync(this::load, Util.nonCriticalIoPool())
                    .thenAcceptAsync(this::upload, minecraft);
        }
    }

    public void upload(NativeImage image) {
        try {
            this.checkOpen();
            if (this.texture == null) {
                this.texture = new DynamicTexture(() -> "Screenshot " + this.textureLocation, image);
            } else {
                this.texture.setPixels(image);
                this.texture.upload();
            }

            this.textureManager.register(this.textureLocation, this.texture);
            loaded = true;
        } catch (Throwable throwable) {
            image.close();
            this.clear();
            throw throwable;
        }
    }


    public void clear() {
        this.checkOpen();
        if (this.texture != null) {
            this.textureManager.release(this.textureLocation);
            this.texture.close();
            this.texture = null;
            this.loaded = false;
        }
    }

    public void close() {
        this.clear();
        this.closed = true;
    }

    public boolean isLoaded() {
        return this.loaded;
    }

    public boolean isClosed() {
        return this.closed;
    }

    private void checkOpen() {
        if (this.closed) {
            throw new IllegalStateException("ScreenshotTexture already closed");
        }
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
}
