package dev.spiritstudios.snapper.gui.overlay;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Overlay;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ExternalDialogOverlay extends Overlay {
    private final MinecraftClient client = MinecraftClient.getInstance();

    @Override
    public boolean pausesGame() {
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawCenteredTextWithShadow(client.textRenderer, Text.translatable("overlay.snapper.external_dialog.folder"), context.getScaledWindowWidth() / 2, context.getScaledWindowHeight() / 2, 0xFFFFFF);

        RenderSystem.enableBlend();
        {
            context.drawTexture(Identifier.of("snapper", "textures/gui/transparent_background.png"), 0, 0, 0, 0, context.getScaledWindowWidth(), context.getScaledWindowHeight());
        }
        RenderSystem.disableBlend();

        if (InputUtil.isKeyPressed(client.getWindow().getHandle(), InputUtil.GLFW_KEY_ESCAPE)) close();
    }

    public void close() {
        this.client.setOverlay(null);
    }
}
