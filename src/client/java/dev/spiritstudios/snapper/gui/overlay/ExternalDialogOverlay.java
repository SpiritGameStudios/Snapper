package dev.spiritstudios.snapper.gui.overlay;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;

public class ExternalDialogOverlay extends Overlay {
    private final Minecraft minecraft = Minecraft.getInstance();

    public static final Identifier MENU_BACKGROUND_TEXTURE = Identifier.withDefaultNamespace("textures/gui/menu_background.png");
    private static final Identifier INWORLD_MENU_BACKGROUND_TEXTURE = Identifier.withDefaultNamespace("textures/gui/inworld_menu_background.png");

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        if (this.minecraft.gui.screen() != null) {
            this.minecraft.gui.screen().extractRenderStateWithTooltipAndSubtitles(graphics, 0, 0, a);
        } else {
            this.minecraft.gui.hud.extractDeferredSubtitles();
        }

        graphics.nextStratum();
        graphics.snapper$forceBlurBeforeThisStratum();

        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                this.minecraft.level == null ? MENU_BACKGROUND_TEXTURE : INWORLD_MENU_BACKGROUND_TEXTURE,
                0, 0,
                0, 0,
                graphics.guiWidth(), graphics.guiHeight(),
                32, 32
        );

        graphics.centeredText(
                minecraft.font,
                Component.translatable("overlay.snapper.external_dialog.folder"),
                graphics.guiWidth() / 2, graphics.guiHeight() / 2,
                CommonColors.WHITE
        );

        if (InputConstants.isKeyDown(minecraft.getWindow(), InputConstants.KEY_ESCAPE)) close();
    }

    public void close() {
        this.minecraft.gui.setOverlay(null);
    }
}
