package dev.spiritstudios.snapper.util;

public class SnapperUtil {
    public static boolean inBoundingBox(int x, int y, int w, int h, double mouseX, double mouseY) {
        return mouseX > x && mouseX < x + w &&
                mouseY > y && mouseY < y + h;
    }
}
