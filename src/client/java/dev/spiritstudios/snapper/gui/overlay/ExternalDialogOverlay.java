package dev.spiritstudios.snapper.gui.overlay;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CommonColors;

public class ExternalDialogOverlay extends Overlay {
    private final Minecraft client = Minecraft.getInstance();

    public static final ResourceLocation MENU_BACKGROUND_TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/menu_background.png");
    private static final ResourceLocation INWORLD_MENU_BACKGROUND_TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/inworld_menu_background.png");

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        if (this.client.screen != null) {
            this.client.screen.renderBackground(context, mouseX, mouseY, delta);
            this.client.screen.render(context, mouseX, mouseY, delta);
        }

        this.client.gameRenderer.processBlurEffect();

        context.blit(
                RenderPipelines.GUI_TEXTURED,
                this.client.level == null ? MENU_BACKGROUND_TEXTURE : INWORLD_MENU_BACKGROUND_TEXTURE,
                0, 0,
                0, 0,
                context.guiWidth(), context.guiHeight(),
                32, 32
        );

        context.drawCenteredString(
                client.font,
                Component.translatable("overlay.snapper.external_dialog.folder"),
                context.guiWidth() / 2, context.guiHeight() / 2,
                CommonColors.WHITE
        );

        if (InputConstants.isKeyDown(client.getWindow(), InputConstants.KEY_ESCAPE)) close();
    }

    public void close() {
        this.client.setOverlay(null);
    }
}
