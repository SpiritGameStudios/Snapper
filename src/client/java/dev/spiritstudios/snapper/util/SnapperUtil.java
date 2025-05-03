package dev.spiritstudios.snapper.util;

import net.minecraft.util.Util;

import java.io.File;
import java.nio.file.Path;

public class SnapperUtil {
    public static boolean inBoundingBox(int x, int y, int w, int h, double mouseX, double mouseY) {
        return mouseX > x && mouseX < x + w &&
                mouseY > y && mouseY < y + h;
    }

    public static Path getOSUnifiedFolder() {
        return switch (Util.getOperatingSystem()) {
            case WINDOWS -> Path.of(System.getenv("APPDATA"), ".snapper");
            case OSX -> Path.of(System.getProperty("user.home") + "/Library/Application Support", "snapper");
            default -> Path.of(System.getProperty("user.home"), ".snapper");
        };
    }
}
