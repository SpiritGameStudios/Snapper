package dev.spiritstudios.snapper.util;

import net.minecraft.client.Minecraft;

public final class SnapperUtil {
    // Helper things. Please order alphabetically. <3 Lynn

    public static boolean inBoundingBox(int x, int y, int w, int h, double mouseX, double mouseY) {
        return mouseX > x && mouseX < x + w && mouseY > y && mouseY < y + h;
    }

    public static boolean isOfflineAccount() {
        return Minecraft.getInstance().getUser().getAccessToken().length() < 400;
    }
}
