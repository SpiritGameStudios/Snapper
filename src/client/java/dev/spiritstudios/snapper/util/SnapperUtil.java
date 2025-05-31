package dev.spiritstudios.snapper.util;

import dev.spiritstudios.snapper.SnapperConfig;
import net.minecraft.client.MinecraftClient;
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

    public static File getConfiguredScreenshotDirectory() {
        File customScreenshotDirectory = new File(SnapperConfig.INSTANCE.customScreenshotFolder.get(), "screenshots");
        File defaultScreenshotDirectory = new File(MinecraftClient.getInstance().runDirectory, "screenshots");

        return SnapperConfig.INSTANCE.useCustomScreenshotFolder.get() ? customScreenshotDirectory : defaultScreenshotDirectory;
    }

    public static boolean isOfflineAccount() {
        return MinecraftClient.getInstance().getSession().getAccessToken().length() < 400;
    }
}
