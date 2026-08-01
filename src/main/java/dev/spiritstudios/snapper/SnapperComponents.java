package dev.spiritstudios.snapper;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public final class SnapperComponents {
    public static final Component CHECK_LOGS = Component.translatable("text.snapper.failure.check_logs");

    public static Component resolution(int x, int y) {
        return Component.translatable("config.snapper.resolution", x, y);
    }

    public static FormattedCharSequence clipTextIfNeeded(final Component text, final Font font, final int width) {
        int textWidth = font.width(text);
        if (textWidth > width) {
            return ComponentRenderUtils.clipText(text, font, width);
        } else {
            return text.getVisualOrderText();
        }
    }
}
