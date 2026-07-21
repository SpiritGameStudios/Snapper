package dev.spiritstudios.snapper;

import net.minecraft.network.chat.Component;

public final class SnapperComponents {
    public static final Component CHECK_LOGS = Component.translatable("text.snapper.failure.check_logs");

    public static Component resolution(int x, int y) {
        return Component.translatable("config.snapper.resolution", x, y);
    }
}
