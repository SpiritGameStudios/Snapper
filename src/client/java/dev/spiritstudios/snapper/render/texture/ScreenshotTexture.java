package dev.spiritstudios.snapper.render.texture;

import com.google.common.hash.Hashing;
import com.mojang.blaze3d.platform.NativeImage;
import dev.spiritstudios.snapper.Snapper;
import dev.spiritstudios.snapper.gui.screen.ScreenshotViewerScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ScreenshotTexture extends GalleryTexture {
    private ScreenshotTexture(TextureManager textureManager, Identifier textureLocation, Path path) {
        super(textureManager, textureLocation, path, Type.SCREENSHOT);
    }

    private DynamicTexture texture;

    @SuppressWarnings("deprecation") // Vanilla uses sha1 for this, copying it
    public static ScreenshotTexture createScreenshot(TextureManager textureManager, Path path) {
        String name = path.getFileName().toString();

        return new ScreenshotTexture(
                textureManager,
                Snapper.id(
                        "screenshots/" + Util.sanitizeName(name, Identifier::validPathChar) + "/" + Hashing.sha1().hashUnencodedChars(name)
                ),
                path
        );
    }

    @Override
    protected NativeImage load() {
        try (InputStream inputStream = Files.newInputStream(this.path)) {
            return NativeImage.read(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void upload(NativeImage image) {
        if (this.isClosed) {
            image.close();
            return;
        }

        this.texture = new DynamicTexture(() -> "Screenshot " + this.textureLocation, image);
        this.textureManager.register(this.textureLocation, this.texture);
        image.close();
    }

    @Override
    public boolean isLoaded() {
        return this.texture != null;
    }

    @Override
    public void clear() {
        if (this.texture != null) {
            this.textureManager.release(textureLocation);
            this.texture.close();
            this.texture = null;
        }

        this.isLoadingStarted = false;
    }

    @Override
    public Screen createViewer(@Nullable Screen parent) {
        return new ScreenshotViewerScreen(this, parent);
    }

    @Override
    public int getWidth() {
        return this.texture != null ? this.texture.getTexture().getWidth(0) : 64;
    }

    @Override
    public int getHeight() {
        return this.texture != null ? this.texture.getTexture().getHeight(0) : 64;
    }
}
