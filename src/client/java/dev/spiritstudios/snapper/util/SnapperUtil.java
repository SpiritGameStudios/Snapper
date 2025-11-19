package dev.spiritstudios.snapper.util;

import dev.spiritstudios.snapper.Snapper;
import dev.spiritstudios.snapper.SnapperConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Util;
import org.apache.commons.lang3.SystemProperties;

import java.nio.file.Files;
import java.nio.file.Path;

public final class SnapperUtil {
    // Helper things. Please order alphabetically. <3 Lynn

    public static Path getConfiguredScreenshotDirectory() {
        if (SnapperConfig.INSTANCE.useCustomScreenshotFolder.get()) {
            Path customPath = SnapperConfig.INSTANCE.customScreenshotFolder.get().resolve("screenshots");

            if (!SafeFiles.createDirectories(customPath)) {
                Snapper.LOGGER.error("Failed to create directories of configured custom screenshot folder");
            }

            return customPath;
        }

        return MinecraftClient.getInstance().runDirectory.toPath().resolve("screenshots");
    }

    public static boolean inBoundingBox(int x, int y, int w, int h, double mouseX, double mouseY) {
        return mouseX > x && mouseX < x + w && mouseY > y && mouseY < y + h;
    }

    public static boolean isOfflineAccount() {
        return MinecraftClient.getInstance().getSession().getAccessToken().length() < 400;
    }

    public static boolean panoramaPresent(Path path) {
        if (!Files.exists(path) || !Files.isDirectory(path)) return false;

        for (int i = 0; i < 6; i++) {
            if (!Files.exists(path.resolve("panorama_%s.png".formatted(i)))) return false;
        }

        return true;
    }

    public enum PanoramaSize {
        ONE_THOUSAND_TWENTY_FOUR(1024), TWO_THOUSAND_FORTY_EIGHT(2048), FOUR_THOUSAND_NINETY_SIX(4096);

        private final int size;

        PanoramaSize(int size) {
            this.size = size;
        }

        public int size() {
            return size;
        }
    }

    public static final Path UNIFIED_FOLDER = switch (Util.getOperatingSystem()) {
        case WINDOWS -> Path.of(System.getenv("APPDATA"), ".snapper");
        case OSX -> Path.of(SystemProperties.getUserHome(), "Library", "Application Support", "snapper");
        default -> Path.of(SystemProperties.getUserHome(), ".snapper");
    };
}
