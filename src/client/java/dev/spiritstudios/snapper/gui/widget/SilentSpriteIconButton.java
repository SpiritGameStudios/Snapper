package dev.spiritstudios.snapper.gui.widget;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class SilentSpriteIconButton extends SpriteIconButton {
    public SilentSpriteIconButton(final int width, final int height, final Component message, final int spriteWidth, final int spriteHeight, final int spriteOffsetX, final int spriteOffsetY, final WidgetSprites sprite, final Button.OnPress onPress, final @Nullable Component tooltip, final Button.@Nullable CreateNarration narration, final boolean switchToLoadingAfterPress) {
        super(width, height, message, spriteWidth, spriteHeight, spriteOffsetX, spriteOffsetY, sprite, onPress, tooltip, narration, switchToLoadingAfterPress);
    }

    @Override
    protected void extractContents(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        if (!this.extractLoadingStateIfLoading(graphics)) {
            this.extractDefaultSprite(graphics);
            int x = this.spriteOffsetX + this.getX() + this.getWidth() / 2 - this.spriteWidth / 2;
            int y = this.spriteOffsetY + this.getY() + this.getHeight() / 2 - this.spriteHeight / 2;
            this.extractSprite(graphics, x, y);
        }
    }

    @Override
    public void playDownSound(final @NonNull SoundManager soundManager) {
    }
}
