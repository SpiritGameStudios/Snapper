package dev.spiritstudios.snapper.util;

import dev.spiritstudios.snapper.Snapper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class ScreenshotIcon implements AutoCloseable {
    private static final Identifier UNKNOWN_SERVER_ID = Identifier.ofVanilla("textures/misc/unknown_server.png");

    private final TextureManager textureManager;
    private final Identifier id;

    @Nullable
    private NativeImageBackedTexture texture;
    private boolean closed;
    private static MinecraftClient client = MinecraftClient.getInstance();

    private ScreenshotIcon(TextureManager textureManager, Identifier id) {
        this.textureManager = textureManager;
        this.id = id;
    }

    private ScreenshotIcon(TextureManager textureManager, Identifier id, File screenshot, boolean FuckIt) {
        this.textureManager = textureManager;
        this.id = id;
        this.loadIcon(screenshot.toPath());
    }

    public static ScreenshotIcon of(File screenshot) {
        return new ScreenshotIcon(
                client.getTextureManager(),
                Identifier.ofVanilla(
                        "screenshots/" + Util.replaceInvalidChars(screenshot.getName(), Identifier::isPathCharacterValid) + "/icon"
                ),
                screenshot,
                true
        );
    }

    private void loadIcon(Path path) {
        CompletableFuture.runAsync(() -> {
            if (path == null || !Files.isRegularFile(path)) {
                return;
            }

            try (InputStream inputStream = Files.newInputStream(path)) {
                this.load(NativeImage.read(inputStream));
            } catch (IOException error) {
                Snapper.LOGGER.error("Invalid icon for screenshot {}", new File(String.valueOf(path)).getName(), error);
            }
        });
    }

    public static ScreenshotIcon forScreenshot(TextureManager textureManager, String screenshotName) {
        return new ScreenshotIcon(
                textureManager,
                Identifier.ofVanilla(
                        "screenshots/" + Util.replaceInvalidChars(screenshotName, Identifier::isPathCharacterValid) + "/icon"
                )
        );
    }

    public static ScreenshotIcon forPanoramaFace(TextureManager textureManager, String screenshotName) {
        return new ScreenshotIcon(
                textureManager,
                Identifier.ofVanilla(
                        "screenshots/panorama/" + Util.replaceInvalidChars(screenshotName, Identifier::isPathCharacterValid)
                )
        );
    }

    public void load(NativeImage image) {
        this.assertOpen();
        if (this.texture == null) this.texture = new NativeImageBackedTexture(image);
        else {
            this.texture.setImage(image);
            this.texture.upload();
        }
        this.textureManager.registerTexture(this.id, this.texture);
    }

    public void joinLoad() {
        // must be called on render thread
        if (this.texture == null) return;
        this.texture.setFilter(true, false);
    }

    public void destroy() {
        this.assertOpen();
        if (this.texture != null) {
            this.textureManager.destroyTexture(this.id);
            this.texture.close();
            this.texture = null;
        }
    }

    public int getWidth() {
        this.assertOpen();
        return this.texture != null && this.texture.getImage() != null ? this.texture.getImage().getWidth() : 64;
    }

    public int getHeight() {
        this.assertOpen();
        return this.texture != null && this.texture.getImage() != null ? this.texture.getImage().getHeight() : 64;
    }

    public Identifier getTextureId() {
        return this.texture != null ? this.id : UNKNOWN_SERVER_ID;
    }

    public void close() {
        this.destroy();
        this.closed = true;
    }

    private void assertOpen() {
        if (this.closed) throw new IllegalStateException("Icon already closed");
    }
}
