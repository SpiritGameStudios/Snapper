package dev.spiritstudios.snapper.render.texture;

import com.google.common.hash.Hashing;
import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import dev.spiritstudios.snapper.Snapper;
import dev.spiritstudios.snapper.gui.screen.PanoramaViewerScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Supplier;

public final class PanoramaTexture extends GalleryTexture {
    private PanoramaTexture(TextureManager textureManager, Identifier textureLocation, Path path) {
        super(textureManager, textureLocation, path);
    }

    @SuppressWarnings("deprecation") // Vanilla uses sha1 for this, copying it
    public static Optional<PanoramaTexture> createScreenshot(TextureManager textureManager, Path path) {
        String name = path.getFileName().toString();

        return Optional.of(new PanoramaTexture(
                textureManager,
                Snapper.id(
                        "screenshots/panoramas/" + Util.sanitizeName(name, Identifier::validPathChar) + "/" + Hashing.sha1().hashUnencodedChars(name)
                ),
                path
        ));
    }

    private Texture texture;

    @Override
    protected NativeImage load() {
        try (InputStream inputStream = Files.newInputStream(path)) {
            NativeImage flippedImage = NativeImage.read(inputStream);
            NativeImage image = new NativeImage(flippedImage.getWidth(), flippedImage.getHeight(), false);
            flippedImage.copyRect(image, 0, 0, 0, 0, flippedImage.getWidth(), flippedImage.getHeight(), false, true);

            return image;
        } catch (IOException e) {
            Snapper.LOGGER.error("Error loading image", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void upload(NativeImage image) {
        if (this.isClosed) {
            image.close();
            return;
        }

        this.texture = new Texture(() -> "Screenshot " + this.textureLocation, image);
        this.textureManager.register(this.textureLocation, this.texture);
        image.close();
        this.isLoaded = true;
    }

    @Override
    public int getWidth() {
        return this.texture != null ? this.texture.getTexture().getWidth(0) : 64;
    }

    @Override
    public int getHeight() {
        return this.texture != null ? this.texture.getTexture().getHeight(0) : 64;
    }

    @Override
    public void close() {
        if (this.texture != null) {
            this.textureManager.release(textureLocation);
            this.texture.close();
            this.texture = null;
        }

        this.isClosed = true;
    }

    @Override
    public Screen createViewer(@Nullable Screen parent) {
        return new PanoramaViewerScreen(this, parent);
    }

    private static class Texture extends AbstractTexture {
        public Texture(Supplier<String> label, NativeImage image) {
            GpuDevice device = RenderSystem.getDevice();
            int width = image.getWidth() / 3;
            int height = image.getHeight() / 2;
            this.close();
            this.texture = device.createTexture(
                    label,
                    GpuTexture.USAGE_COPY_DST | GpuTexture.USAGE_TEXTURE_BINDING | GpuTexture.USAGE_CUBEMAP_COMPATIBLE,
                    GpuFormat.RGBA8_UNORM,
                    width, height,
                    6, 1
            );
            this.textureView = device.createTextureView(this.texture);
            GpuBufferSlice stagingBuffer = device.createCommandEncoder().transientMemory().uploadStaging(image.getPixelBytes(), 1L, GpuBuffer.USAGE_COPY_SRC);


            // RIGHT
            device.createCommandEncoder().copyBufferToTexture(
                    stagingBuffer,
                    width * 2, 0,
                    image.getWidth(), image.getHeight(),
                    this.texture,
                    0, 0,
                    width, height,
                    0, 0
            );

            // LEFT
            device.createCommandEncoder().copyBufferToTexture(
                    stagingBuffer,
                    0, 0,
                    image.getWidth(), image.getHeight(),
                    this.texture,
                    0, 0,
                    width, height,
                    0, 1
            );

            // BOTTOM
            device.createCommandEncoder().copyBufferToTexture(
                    stagingBuffer,
                    0, height,
                    image.getWidth(), image.getHeight(),
                    this.texture,
                    0, 0,
                    width, height,
                    0, 2
            );

            // TOP
            device.createCommandEncoder().copyBufferToTexture(
                    stagingBuffer,
                    width, height,
                    image.getWidth(), image.getHeight(),
                    this.texture,
                    0, 0,
                    width, height,
                    0, 3
            );

            // FRONT
            device.createCommandEncoder().copyBufferToTexture(
                    stagingBuffer,
                    width, 0,
                    image.getWidth(), image.getHeight(),
                    this.texture,
                    0, 0,
                    width, height,
                    0, 4
            );

            // BACK
            device.createCommandEncoder().copyBufferToTexture(
                    stagingBuffer,
                    width * 2, height,
                    image.getWidth(), image.getHeight(),
                    this.texture,
                    0, 0,
                    width, height,
                    0, 5
            );
        }
    }
}
