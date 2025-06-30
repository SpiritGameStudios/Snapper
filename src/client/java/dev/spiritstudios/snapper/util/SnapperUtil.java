package dev.spiritstudios.snapper.util;

import dev.spiritstudios.snapper.SnapperConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Util;
import org.apache.commons.lang3.SystemProperties;

import java.nio.file.Path;

public class SnapperUtil {
    public static boolean inBoundingBox(int x, int y, int w, int h, double mouseX, double mouseY) {
        return mouseX > x && mouseX < x + w &&
                mouseY > y && mouseY < y + h;
    }

    public static Path getOSUnifiedFolder() {
        return switch (Util.getOperatingSystem()) {
            case WINDOWS -> Path.of(System.getenv("APPDATA"), ".snapper");
            case OSX -> Path.of(SystemProperties.getUserHome() + "/Library/Application Support", "snapper");
            default -> Path.of(SystemProperties.getUserHome(), ".snapper");
        };
    }

    public static Path getConfiguredScreenshotDirectory() {
        return SnapperConfig.INSTANCE.useCustomScreenshotFolder.get() ?
                SnapperConfig.INSTANCE.customScreenshotFolder.get().resolve("screenshots") :
                MinecraftClient.getInstance().runDirectory.toPath().resolve("screenshots");
    }

    public static boolean isOfflineAccount() {
        return MinecraftClient.getInstance().getSession().getAccessToken().length() < 400;
    }
}
