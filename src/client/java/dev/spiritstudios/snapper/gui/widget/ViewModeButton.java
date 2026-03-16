package dev.spiritstudios.snapper.gui.widget;

import dev.spiritstudios.snapper.Snapper;
import dev.spiritstudios.snapper.SnapperConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

public class ViewModeButton extends Button {
    private static final int SPRITE_SIZE = 15;

    private static final WidgetSprites GRID_SPRITES = new WidgetSprites(Snapper.id("screenshots/show_grid"));
    private static final WidgetSprites LIST_SPRITES = new WidgetSprites(Snapper.id("screenshots/show_list"));

    public ViewModeButton(OnPress onPress, @Nullable CreateNarration createNarration) {
        super(0, 0, 20, 20, Component.translatable("config.snapper.viewMode"), onPress, createNarration == null ? DEFAULT_NARRATION : createNarration);
    }

    @Override
    protected void renderContents(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderDefaultSprite(graphics);
        int x = this.getX() + this.getWidth() / 2 - SPRITE_SIZE / 2;
        int y = this.getY() + this.getHeight() / 2 - SPRITE_SIZE / 2;

        WidgetSprites sprite = switch (SnapperConfig.HOLDER.get().viewMode()) {
            case LIST -> LIST_SPRITES;
            case GRID -> GRID_SPRITES;
        };

        graphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                sprite.get(this.isActive(), this.isHoveredOrFocused()),
                x, y,
                SPRITE_SIZE, SPRITE_SIZE,
                this.alpha
        );
    }
}
