package dev.spiritstudios.snapper.gui.screen;

import dev.spiritstudios.snapper.Snapper;
import dev.spiritstudios.snapper.util.DynamicCubemapTexture;
import dev.spiritstudios.snapper.util.SafeFiles;
import dev.spiritstudios.snapper.util.SnapperUtil;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.client.renderer.state.gui.PanoramaRenderState;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class PanoramaViewerScreen extends Screen {
    public static final RenderStateDataKey<PanoramaRenderState> SNAPPER_PANORAMA = RenderStateDataKey.create(() -> "Snapper Panorama");

    public static final Identifier TEXTURE_ID = Snapper.id("screenshots/panorama");

    private final DynamicCubemapTexture texture;

    private final String title;
    private final Screen parent;

    private float spin = 0.0F;

    public PanoramaViewerScreen(String title, Screen parent) {
        super(Component.translatable("menu.snapper.viewer_menu"));
        this.title = title;
        this.parent = parent;
        this.texture = this.getTexture();

        if (texture != null) {
            // TODO: May be worth doing texture loading here off-thread as not to cause a freeze
            Minecraft.getInstance().getTextureManager().registerAndLoad(TEXTURE_ID, texture);
        }
    }

    @Nullable
    private DynamicCubemapTexture getTexture() {
        Path panoramaDir = SnapperUtil.getConfiguredScreenshotDirectory().resolve("panorama");
        if (!SnapperUtil.panoramaPresent(panoramaDir)) return null;

        try (Stream<Path> stream = Files.list(panoramaDir)) {
            return stream.allMatch(path -> {
                if (Files.isDirectory(path)) return false;

                return SafeFiles.isContentType(path, "image/png", ".png");
            }) ? new DynamicCubemapTexture(TEXTURE_ID, panoramaDir) : null;
        } catch (IOException | NullPointerException e) {
            Snapper.LOGGER.error("Failed to list the contents of directory", e);
            return null;
        }
    }

    @Override
    public void onClose() {
        if (texture != null) {
            Minecraft.getInstance().getTextureManager().release(TEXTURE_ID);
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

        Path panoramaPath = SnapperUtil.getConfiguredScreenshotDirectory().resolve("screenshots", "panorama");
        addRenderableWidget(Button.builder(Component.translatable("button.snapper.folder"), _ -> {
            Util.getPlatform().openPath(panoramaPath);
        }).bounds(width / 2 - 150 - 4, height - 32, 150, 20).build());

        addRenderableWidget(Button.builder(
                CommonComponents.GUI_DONE,
                button -> this.onClose()
        ).bounds(width / 2 + 4, height - 32, 150, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        Minecraft minecraft = Minecraft.getInstance();
        float delta = (float) ((double) a * minecraft.gameRenderer.getGameRenderState().optionsRenderState.panoramaSpeed);
        this.spin = Mth.wrapDegrees(this.spin + delta * 0.1F);

        minecraft.gameRenderer.getGameRenderState().guiRenderState.setData(SNAPPER_PANORAMA, new PanoramaRenderState(-this.spin));

        graphics.centeredText(
                this.font,
                this.title,
                this.width / 2,
                20,
                CommonColors.WHITE
        );

        super.extractRenderState(graphics, mouseX, mouseY, a);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
    }
}
