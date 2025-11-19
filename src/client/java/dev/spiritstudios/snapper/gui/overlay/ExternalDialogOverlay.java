package dev.spiritstudios.snapper.gui.overlay;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Overlay;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;

public class ExternalDialogOverlay extends Overlay {
    private final MinecraftClient client = MinecraftClient.getInstance();

    public static final Identifier MENU_BACKGROUND_TEXTURE = Identifier.ofVanilla("textures/gui/menu_background.png");
    private static final Identifier INWORLD_MENU_BACKGROUND_TEXTURE = Identifier.ofVanilla("textures/gui/inworld_menu_background.png");

    @Override
    public boolean pausesGame() {
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.client.currentScreen != null) {
            this.client.currentScreen.renderBackground(context, mouseX, mouseY, delta);
            this.client.currentScreen.render(context, mouseX, mouseY, delta);
        }

        this.client.gameRenderer.renderBlur();

        context.drawTexture(
                RenderPipelines.GUI_TEXTURED,
                this.client.world == null ? MENU_BACKGROUND_TEXTURE : INWORLD_MENU_BACKGROUND_TEXTURE,
                0, 0,
                0, 0,
                context.getScaledWindowWidth(), context.getScaledWindowHeight(),
                32, 32
        );

        context.drawCenteredTextWithShadow(
                client.textRenderer,
                Text.translatable("overlay.snapper.external_dialog.folder"),
                context.getScaledWindowWidth() / 2, context.getScaledWindowHeight() / 2,
                Colors.WHITE
        );

        if (InputUtil.isKeyPressed(client.getWindow(), InputUtil.GLFW_KEY_ESCAPE)) close();
    }

    public void close() {
        this.client.setOverlay(null);
    }
}
