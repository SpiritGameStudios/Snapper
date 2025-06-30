package dev.spiritstudios.snapper.gui.screen;

import dev.spiritstudios.snapper.Snapper;
import dev.spiritstudios.snapper.util.SafeFiles;
import dev.spiritstudios.snapper.util.ScreenshotImage;
import net.minecraft.client.gui.CubeMapRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.RotatingCubeMapRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class PanoramaViewerScreen extends Screen {
    protected static final CubeMapRenderer PANORAMA_RENDERER = new CubeMapRenderer(Identifier.ofVanilla("screenshots/panorama/panorama"));
    protected static final CubeMapRenderer FALLBACK_PANORAMA_RENDERER = new CubeMapRenderer(Identifier.ofVanilla("textures/gui/title/background/panorama"));
    protected static final RotatingCubeMapRenderer PANORAMA_RENDERER_CUBE = new RotatingCubeMapRenderer(PANORAMA_RENDERER);
    protected static final RotatingCubeMapRenderer FALLBACK_PANORAMA_RENDERER_CUBE = new RotatingCubeMapRenderer(FALLBACK_PANORAMA_RENDERER);

    private final String title;
    private final Screen parent;
    private boolean doBackgroundFade = true;
    private long backgroundFadeStart;
    private boolean loaded;
    private float backgroundAlpha;

    protected PanoramaViewerScreen(String title, Screen parent) {
        super(Text.translatable("menu.snapper.viewer_menu"));
        this.title = title;
        this.parent = parent;
    }

    private void load() {
        List<Path> panorama = this.getImagePaths();
        if (panorama == null) return;

        assert this.client != null;

        for (Path path : panorama) {
            ScreenshotImage.createPanoramaFace(this.client.getTextureManager(), path)
                    .ifPresent(ScreenshotImage::enableFiltering);
        }

        this.loaded = true;
    }

    @Nullable
    private List<Path> getImagePaths() {
        Objects.requireNonNull(this.client);

        Path panoramaDir = Path.of(this.client.runDirectory.getPath(), "screenshots", "panorama");
        if (!Files.exists(panoramaDir)) return null;

        try (Stream<Path> stream = Files.list(panoramaDir)) {
            return stream
                    .filter(path -> {
                        if (Files.isDirectory(path)) return false;

                        return SafeFiles.probeContentType(path)
                                .map(fileType -> Objects.equals(fileType, "image/png"))
                                .orElse(false);
                    })
                    .toList();
        } catch (IOException e) {
            Snapper.LOGGER.error("Failed to list the contents of directory", e);
            return Collections.emptyList();
        }
    }

    @Override
    public void close() {
        Objects.requireNonNull(this.client);

        client.setScreen(this.parent);
    }

    @Override
    protected void init() {
        assert client != null;

        Path panoramaPath = Path.of(client.runDirectory.getPath(), "screenshots", "panorama");
        addDrawableChild(ButtonWidget.builder(Text.translatable("button.snapper.folder"), button -> {
            if (!SafeFiles.createDirectories(panoramaPath)) {
                Snapper.LOGGER.error("Failed to create directory \"{}\"", panoramaPath);
                client.setScreen(parent);
                return;
            }

            Util.getOperatingSystem().open(panoramaPath);
        }).dimensions(width / 2 - 150 - 4, height - 32, 150, 20).build());

        addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.close()).dimensions(width / 2 + 4, height - 32, 150, 20).build());

        this.load();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.backgroundFadeStart == 0L && this.doBackgroundFade)
            this.backgroundFadeStart = Util.getMeasuringTimeMs();

        if (doBackgroundFade) {
            float progress = (float) (Util.getMeasuringTimeMs() - this.backgroundFadeStart) / 2000.0F;
            float widgetProgress = 1.0F;

            if (progress > 1.0F) {
                this.doBackgroundFade = false;
                backgroundAlpha = 1.0F;
            } else {
                progress = MathHelper.clamp(progress, 0.0F, 1.0F);
                widgetProgress = MathHelper.clampedMap(progress, 0.0F, 0.5F, 0.0F, 1.0F);
                backgroundAlpha = MathHelper.clampedMap(progress, 0.0F, 0.5F, 0.0F, 1.0F);
            }

            this.setWidgetOpacity(widgetProgress);
        }

        super.render(context, mouseX, mouseY, delta);

        if (this.loaded) this.renderPanoramaBackground(context, delta);
        else {
            FALLBACK_PANORAMA_RENDERER_CUBE.render(context, this.width, this.height, this.backgroundAlpha, delta);
            context.drawCenteredTextWithShadow(textRenderer, Text.translatable("text.snapper.panorama_encourage"), this.width / 2, this.height / 2, 0xFFFFFF);
        }

        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xffffff);
    }

    private void setWidgetOpacity(float alpha) {
        for (Element element : this.children()) {
            if (element instanceof ClickableWidget clickableWidget) {
                clickableWidget.setAlpha(alpha);
            }
        }
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
    }

    @Override
    protected void renderPanoramaBackground(DrawContext context, float delta) {
        if (!this.loaded) return;
        PANORAMA_RENDERER_CUBE.render(context, this.width, this.height, this.backgroundAlpha, delta);
    }
}
