package dev.spiritstudios.snapper.util;

import com.mojang.blaze3d.platform.NativeImage;
import dev.spiritstudios.snapper.Snapper;
import net.minecraft.client.renderer.texture.CubeMapTexture;
import net.minecraft.client.renderer.texture.TextureContents;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class DynamicCubemapTexture extends CubeMapTexture {
    private static final String[] TEXTURE_SUFFIXES = new String[]{"_1.png", "_3.png", "_5.png", "_4.png", "_0.png", "_2.png"};
    private final Path path;

    public DynamicCubemapTexture(ResourceLocation id, Path path) {
        super(id);
        this.path = path;
    }

    @Override
    public TextureContents loadContents(ResourceManager resourceManager) throws IOException {
        TextureContents contents;
        try (InputStream baseStream = Files.newInputStream(path.resolve("panorama" + TEXTURE_SUFFIXES[0]))) {
            NativeImage baseImage = NativeImage.read(baseStream);
            int width = baseImage.getWidth();
            int height = baseImage.getHeight();
            NativeImage image = new NativeImage(width, height * 6, false);
            baseImage.copyRect(image, 0, 0, 0, 0, width, height, false, true);

            for (int i = 1; i < 6; i++) {
                try (InputStream panoramaStream = Files.newInputStream(path.resolve("panorama" + TEXTURE_SUFFIXES[i]))) {
                    NativeImage panoramaImage = NativeImage.read(panoramaStream);
                    if (panoramaImage.getWidth() != width || panoramaImage.getHeight() != height) {
                        Snapper.LOGGER.error("Image dimensions of panorama '{}' sides do not match: part 0 is {}x{}, but part {} is {}x{}", getTexture(), width, height, i, panoramaImage.getWidth(), panoramaImage.getHeight());
                        baseImage.close();
                        throw new IOException();
                    }
                    panoramaImage.copyRect(image, 0, 0, 0, i * height, width, height, false, true);
                    panoramaImage.close();
                }
            }

            baseImage.close();
            contents = new TextureContents(image, new TextureMetadataSection(true, false));
        }
        return contents;
    }

    public static Optional<DynamicCubemapTexture> createPanorama(ResourceLocation id, Path path) {
        return Optional.of(new DynamicCubemapTexture(
                id,
                path
        ));
    }
}
