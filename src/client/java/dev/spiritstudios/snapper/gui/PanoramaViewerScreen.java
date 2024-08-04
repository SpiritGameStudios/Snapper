package dev.spiritstudios.snapper.gui;

import dev.spiritstudios.snapper.Snapper;
import dev.spiritstudios.snapper.util.ScreenshotIcon;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.CubeMapRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.RotatingCubeMapRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static dev.spiritstudios.snapper.Snapper.MODID;

public class PanoramaViewerScreen extends Screen {
    protected static final CubeMapRenderer PANORAMA_RENDERER = new CubeMapRenderer(Identifier.ofVanilla("screenshots/panorama/panorama"));
    protected static final CubeMapRenderer FALLBACK_PANORAMA_RENDERER = new CubeMapRenderer(Identifier.ofVanilla("textures/gui/title/background/panorama"));
    protected static final RotatingCubeMapRenderer PANORAMA_RENDERER_CUBE = new RotatingCubeMapRenderer(PANORAMA_RENDERER);
    protected static final RotatingCubeMapRenderer FALLBACK_PANORAMA_RENDERER_CUBE = new RotatingCubeMapRenderer(FALLBACK_PANORAMA_RENDERER);
    private static final MinecraftClient client = MinecraftClient.getInstance();

    private final Path iconPath;
    private final String title;
    private boolean doBackgroundFade = true;
    private long backgroundFadeStart;
    private boolean loaded = false;
    private float backgroundAlpha;
    private Screen parent;

    protected PanoramaViewerScreen(String title, Screen parent) {
        super(Text.translatable("menu.snapper.viewermenu"));
        this.title = title;
        this.parent = parent;
        this.iconPath = new File(client.runDirectory + "screenshots/panorama").toPath();
        this.load();
    }

    private void load() {
        List<File> panorama = this.loadPanorama();
        if (panorama == null) return;

        panorama.parallelStream().forEach(face -> {
            ScreenshotIcon icon = ScreenshotIcon.forPanoramaFace(client.getTextureManager(), face.getName());
            this.loadIcon(icon, face.getName(), Path.of(face.getPath()));
        });

        this.loaded = true;
    }

    private void loadIcon(ScreenshotIcon icon, String fileName, Path filePath) {
        if (filePath == null || !Files.isRegularFile(filePath)) {
            icon.destroy();
            return;
        }

        try (InputStream inputStream = Files.newInputStream(filePath)) {
            icon.load(NativeImage.read(inputStream));
        } catch (IOException error) {
            Snapper.LOGGER.error("Invalid face for panorama {}", fileName, error);
        }
    }

    @Nullable
    private List<File> loadPanorama() {
        File panoramaDir = new File(client.runDirectory, "screenshots/panorama");
        List<File> panoramaFaces;
        if (!Files.exists(panoramaDir.toPath())) return null;

        File[] faceFiles = panoramaDir.listFiles();
        panoramaFaces = new ArrayList<>(List.of(faceFiles == null ? new File[0] : faceFiles));
        panoramaFaces.removeIf(file -> {
            if (Files.isDirectory(file.toPath())) return true;
            String fileType;

            try {
                fileType = Files.probeContentType(file.toPath());
            } catch (IOException e) {
                Snapper.LOGGER.error("Couldn't load panorama list", e);
                return true;
            }

            return !Objects.equals(fileType, "image/png");
        });

        return panoramaFaces;
    }

    @Override
    public void close() {
        client.setScreen(this.parent);
    }

    @Override
    protected void init() {
        File panoramaDirectory = new File(client.runDirectory, "screenshots/panorama");
        addDrawableChild(ButtonWidget.builder(Text.translatable("button.snapper.folder"), button -> {
            if (!panoramaDirectory.exists()) {
                new File(String.valueOf(panoramaDirectory)).mkdirs();
                Util.getOperatingSystem().open(panoramaDirectory);
            } else {
                Util.getOperatingSystem().open(panoramaDirectory);
            }
        })
                .dimensions(width / 2 - 150 - 2, height - 32, 150, 20)
                .build()
        );

        addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.close())
                .dimensions(width / 2 + 2, height - 32, 150, 20)
                .build()
        );
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.backgroundFadeStart == 0L && this.doBackgroundFade)
            this.backgroundFadeStart = Util.getMeasuringTimeMs();

        if (doBackgroundFade) {
            float progress = (float) (Util.getMeasuringTimeMs() - this.backgroundFadeStart) / 2000.0F;

            if (progress > 1.0F) {
                this.doBackgroundFade = false;
                backgroundAlpha = 1.0F;
            } else {
                progress = MathHelper.clamp(progress, 0.0F, 1.0F);
                backgroundAlpha = MathHelper.clampedMap(progress, 0.0F, 0.5F, 0.0F, 1.0F);
            }
        }

        super.render(context, mouseX, mouseY, delta);

        if (this.loaded) this.renderPanoramaBackground(context, delta);
        else {
            FALLBACK_PANORAMA_RENDERER_CUBE.render(context, this.width, this.height, this.backgroundAlpha, delta);
            context.drawCenteredTextWithShadow(textRenderer, Text.translatable("text.snapper.panorama_encourage"), this.width / 2, this.height / 2, 0xFFFFFF);
        }
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xffffff);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) { }

    @Override
    protected void renderPanoramaBackground(DrawContext context, float delta) {
        if (!this.loaded) return;
        PANORAMA_RENDERER_CUBE.render(context, this.width, this.height, this.backgroundAlpha, delta);
    }
}
