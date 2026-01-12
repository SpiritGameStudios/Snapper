package dev.spiritstudios.snapper.gui.screen;

import dev.spiritstudios.snapper.Snapper;
import dev.spiritstudios.snapper.util.DynamicCubemapTexture;
import dev.spiritstudios.snapper.util.SafeFiles;
import dev.spiritstudios.snapper.util.SnapperUtil;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.CubeMap;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CommonColors;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class PanoramaViewerScreen extends Screen {
    protected static final ResourceLocation ID = Snapper.id("screenshots/panorama");
    protected static final CubeMap PANORAMA_RENDERER = new CubeMap(ID);

    private final PanoramaRenderer rotatingPanoramaRenderer = new PanoramaRenderer(PANORAMA_RENDERER);
    private final DynamicCubemapTexture texture;

    private final String title;
    private final Screen parent;

    protected PanoramaViewerScreen(String title, Screen parent) {
        super(Component.translatable("menu.snapper.viewer_menu"));
        this.title = title;
        this.parent = parent;
        this.texture = this.getTexture();
        if (texture != null) {
            Minecraft.getInstance().getTextureManager().registerAndLoad(ID, texture);
        }
    }

    @Nullable
    private DynamicCubemapTexture getTexture() {
		assert minecraft != null;

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
    public void onClose() {
        if (texture != null) {
            Minecraft.getInstance().getTextureManager().release(ID);
            texture.close();
        }

        Minecraft.getInstance().setScreen(this.parent);
    }

    @Override
    protected void init() {
        // This is called whenever the window is resized.
        if (this.texture == null) {
            Snapper.LOGGER.error("No panorama found");
            onClose();
            return;
        }

        Path panoramaPath = Path.of(Minecraft.getInstance().gameDirectory.getPath(), "screenshots", "panorama");
        addRenderableWidget(Button.builder(Component.translatable("button.snapper.folder"), button -> {
            Util.getPlatform().openPath(panoramaPath);
        }).bounds(width / 2 - 150 - 4, height - 32, 150, 20).build());

        addRenderableWidget(Button.builder(
                CommonComponents.GUI_DONE,
                button -> this.onClose()
        ).bounds(width / 2 + 4, height - 32, 150, 20).build());
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        rotatingPanoramaRenderer.render(context, this.width, this.height, true);

        context.drawCenteredString(
                this.font,
                this.title,
                this.width / 2,
                20,
                CommonColors.WHITE
        );

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
    }
}
