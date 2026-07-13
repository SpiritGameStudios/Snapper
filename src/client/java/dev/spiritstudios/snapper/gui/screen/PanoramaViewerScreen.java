package dev.spiritstudios.snapper.gui.screen;

import dev.spiritstudios.snapper.Snapper;
import dev.spiritstudios.snapper.render.panorama.GuiPanoramaRenderState;
import dev.spiritstudios.snapper.render.texture.PanoramaTexture;
import dev.spiritstudios.snapper.util.SnapperUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;

import java.nio.file.Path;

public class PanoramaViewerScreen extends Screen {
    private final PanoramaTexture texture;

    private final Screen parent;

    private float spin = 0.0F;

    public PanoramaViewerScreen(PanoramaTexture texture, Screen parent) {
        super(Component.translatable("menu.snapper.viewer_menu"));
        this.parent = parent;
        this.texture = texture;
    }

    @Override
    public void onClose() {
        if (!(parent instanceof GalleryScreen)) {
            this.texture.close();
        }

        this.minecraft.gui.setScreen(this.parent);
    }

    @Override
    protected void init() {
        // This is called whenever the window is resized.
        if (this.texture == null) {
            Snapper.LOGGER.error("No panorama found");
            onClose();
            return;
        }

        addRenderableWidget(Button.builder(Component.translatable("button.snapper.folder"), _ -> {
            Util.getPlatform().openPath(texture.path);
        }).bounds(width / 2 - 150 - 4, height - 32, 150, 20).build());

        addRenderableWidget(Button.builder(
                CommonComponents.GUI_DONE,
                _ -> this.onClose()
        ).bounds(width / 2 + 4, height - 32, 150, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        float delta = (float) ((double) a * minecraft.gameRenderer.gameRenderState().optionsRenderState.panoramaSpeed);
        this.spin = Mth.wrapDegrees(this.spin + delta * 0.1F);

        texture.startLoading(minecraft, false);
        if (texture.isLoaded()) {
            graphics.snapper$panorama(
                    -this.spin,
                    this.texture.textureLocation,
                    0, 0,
                    graphics.guiWidth(), graphics.guiHeight()
            );
        }

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
