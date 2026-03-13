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
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (this.client.screen != null) {
            this.client.screen.renderWithTooltipAndSubtitles(graphics, mouseX, mouseY, partialTick);
        }

        graphics.nextStratum();

        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                this.client.level == null ? MENU_BACKGROUND_TEXTURE : INWORLD_MENU_BACKGROUND_TEXTURE,
                0, 0,
                0, 0,
                graphics.guiWidth(), graphics.guiHeight(),
                32, 32
        );

        graphics.drawCenteredString(
                client.font,
                Component.translatable("overlay.snapper.external_dialog.folder"),
                graphics.guiWidth() / 2, graphics.guiHeight() / 2,
                CommonColors.WHITE
        );

        if (InputConstants.isKeyDown(client.getWindow(), InputConstants.KEY_ESCAPE)) close();
    }

    public void close() {
        this.client.setOverlay(null);
    }
}
