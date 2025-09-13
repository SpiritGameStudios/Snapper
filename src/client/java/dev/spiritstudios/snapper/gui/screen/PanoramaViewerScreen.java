package dev.spiritstudios.snapper.gui.screen;

import dev.spiritstudios.snapper.Snapper;
import dev.spiritstudios.snapper.util.DynamicTexture;
import dev.spiritstudios.snapper.util.SafeFiles;
import dev.spiritstudios.snapper.util.SnapperUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.CubeMapRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.RotatingCubeMapRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class PanoramaViewerScreen extends Screen {
    protected static final CubeMapRenderer PANORAMA_RENDERER = new CubeMapRenderer(Snapper.id("screenshots/panorama/panorama"));

    protected static final RotatingCubeMapRenderer ROTATING_PANORAMA_RENDERER = new RotatingCubeMapRenderer(PANORAMA_RENDERER);

    private final String title;
    private final Screen parent;

    private final List<DynamicTexture> images = new ArrayList<>();

    protected PanoramaViewerScreen(String title, Screen parent) {
        super(Text.translatable("menu.snapper.viewer_menu"));
        this.title = title;
        this.parent = parent;
        this.client = MinecraftClient.getInstance();
        assert this.client != null;

        List<Path> facePaths = this.getImagePaths();

        if (facePaths == null) {
            Snapper.LOGGER.error("No panorama found");
            close();
            return;
        }

        for (Path path : facePaths) {
            DynamicTexture.createPanoramaFace(this.client.getTextureManager(), path)
                    .ifPresent(screenshotImage -> {
                        images.add(screenshotImage);
                        screenshotImage
                                .load()
                                .thenAccept(ignored -> screenshotImage.enableFiltering());
                    });
        }
    }


    private @Nullable @Unmodifiable List<Path> getImagePaths() {
        Objects.requireNonNull(this.client);

        Path panoramaDir = SnapperUtil.getConfiguredScreenshotDirectory().resolve("panorama");
        if (!SnapperUtil.panoramaPresent(panoramaDir)) return null;

        try (Stream<Path> stream = Files.list(panoramaDir)) {
            return stream
                    .filter(path -> {
                        if (Files.isDirectory(path)) return false;

                        return SafeFiles.isContentType(path, "image/png", ".png");
                    })
                    .toList();
        } catch (IOException | NullPointerException e) {
            Snapper.LOGGER.error("Failed to list the contents of directory", e);
            return null;
        }
    }

    @Override
    public void close() {
        Objects.requireNonNull(this.client);

        for (DynamicTexture image : images) {
            image.close();
        }

        client.setScreen(this.parent);
    }

    @Override
    protected void init() {
        assert client != null;

        Path panoramaPath = Path.of(client.runDirectory.getPath(), "screenshots", "panorama");
        addDrawableChild(ButtonWidget.builder(Text.translatable("button.snapper.folder"), button -> {
            Util.getOperatingSystem().open(panoramaPath);
        }).dimensions(width / 2 - 150 - 4, height - 32, 150, 20).build());

        addDrawableChild(ButtonWidget.builder(
                ScreenTexts.DONE,
                button -> this.close()
        ).dimensions(width / 2 + 4, height - 32, 150, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ROTATING_PANORAMA_RENDERER.render(context, this.width, this.height, true);

        context.drawCenteredTextWithShadow(
                this.textRenderer,
                this.title,
                this.width / 2,
                20,
                Colors.WHITE
        );

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
    }
}
