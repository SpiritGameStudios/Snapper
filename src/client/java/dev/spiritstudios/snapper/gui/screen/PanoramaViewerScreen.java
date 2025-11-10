package dev.spiritstudios.snapper.gui.screen;

import dev.spiritstudios.snapper.Snapper;
import dev.spiritstudios.snapper.util.DynamicCubemapTexture;
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
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class PanoramaViewerScreen extends Screen {
    protected static final Identifier ID = Snapper.id("screenshots/panorama");
    protected static final CubeMapRenderer PANORAMA_RENDERER = new CubeMapRenderer(ID);

    private final RotatingCubeMapRenderer rotatingPanoramaRenderer = new RotatingCubeMapRenderer(PANORAMA_RENDERER);
    private final DynamicCubemapTexture texture;

    private final String title;
    private final Screen parent;

    protected PanoramaViewerScreen(String title, Screen parent) {
        super(Text.translatable("menu.snapper.viewer_menu"));
        this.title = title;
        this.parent = parent;
        this.client = MinecraftClient.getInstance();
        assert client != null;
        this.texture = this.getTexture();
        if (texture != null) {
            client.getTextureManager().registerTexture(ID, texture);
        }
    }

    @Nullable
    private DynamicCubemapTexture getTexture() {
		assert client != null;

        Path panoramaDir = SnapperUtil.getConfiguredScreenshotDirectory().resolve("panorama");
        if (!SnapperUtil.panoramaPresent(panoramaDir)) return null;

        try (Stream<Path> stream = Files.list(panoramaDir)) {
            return stream
                    .allMatch(path -> {
                        if (Files.isDirectory(path)) return false;

                        return SafeFiles.isContentType(path, "image/png", ".png");
                    }) ? DynamicCubemapTexture.createPanorama(ID, panoramaDir).orElse(null) : null;
        } catch (IOException | NullPointerException e) {
            Snapper.LOGGER.error("Failed to list the contents of directory", e);
            return null;
        }
    }

    @Override
    public void close() {
		assert client != null;

        if (texture != null) {
            client.getTextureManager().destroyTexture(ID);
            texture.close();
        }

        client.setScreen(this.parent);
    }

    @Override
    protected void init() {
        // This is called whenever the window is resized.
        assert client != null;

        if (this.texture == null) {
            Snapper.LOGGER.error("No panorama found");
            close();
            return;
        }

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
        rotatingPanoramaRenderer.render(context, this.width, this.height, true);

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
