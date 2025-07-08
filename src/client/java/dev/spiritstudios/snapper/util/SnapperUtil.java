package dev.spiritstudios.snapper.util;

import dev.spiritstudios.snapper.Snapper;
import dev.spiritstudios.snapper.SnapperConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Util;
import org.apache.commons.lang3.SystemProperties;

import java.io.IOException;
import java.nio.file.Files;
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
        if (SnapperConfig.INSTANCE.useCustomScreenshotFolder.get()) {
            Path customPath = SnapperConfig.INSTANCE.customScreenshotFolder.get().resolve("screenshots");
            try {
                Files.createDirectories(customPath);
            } catch (IOException e) {
                Snapper.LOGGER.error("Failed to create directories of configured custom screenshot folder");
            }
            return customPath;
        }
        return MinecraftClient.getInstance().runDirectory.toPath().resolve("screenshots");
    }

    public static boolean isOfflineAccount() {
        return MinecraftClient.getInstance().getSession().getAccessToken().length() < 400;
    }

    public static boolean panoramaPresent(Path path) {
        if (!Files.exists(path)) return false;
        int partsPresent = 0;

        for (int i = 0; i < 6; i++) {
            if (Files.exists(path.resolve("panorama_%s.png".formatted(i)))) partsPresent++;
        }

        return partsPresent == 6;
    }
}
