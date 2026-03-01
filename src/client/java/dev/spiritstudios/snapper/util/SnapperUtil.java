package dev.spiritstudios.snapper.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.spiritstudios.snapper.Snapper;
import dev.spiritstudios.snapper.SnapperConfig;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import org.apache.commons.lang3.SystemProperties;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;

public final class SnapperUtil {
    // Helper things. Please order alphabetically. <3 Lynn

    public static Path getConfiguredScreenshotDirectory() {
        if (SnapperConfig.HOLDER.get().customScreenshotPath().enabled()) {
            Path customPath = SnapperConfig.HOLDER.get().customScreenshotPath().path().resolve("screenshots");

            if (!SafeFiles.createDirectories(customPath)) {
                Snapper.LOGGER.error("Failed to create directories of configured custom screenshot folder");
            }

            return customPath;
        }

        return Minecraft.getInstance().gameDirectory.toPath().resolve("screenshots");
    }

    public static boolean inBoundingBox(int x, int y, int w, int h, double mouseX, double mouseY) {
        return mouseX > x && mouseX < x + w && mouseY > y && mouseY < y + h;
    }

    public static boolean isOfflineAccount() {
        return Minecraft.getInstance().getUser().getAccessToken().length() < 400;
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

        public static final Codec<PanoramaSize> CODEC = Codec.INT.comapFlatMap(
                i -> {
                    for (PanoramaSize size : PanoramaSize.values()) {
                        if (i == size.size) {
                            return DataResult.success(size);
                        }
                    }
                    return DataResult.error(() -> "Invalid panorama size, must be one of " + Arrays.stream(PanoramaSize.values())
                            .map(panoramaSize -> Integer.toString(panoramaSize.size))
                            .collect(Collectors.joining(","))
                    );
                },
                PanoramaSize::size
        );
        private final int size;

        PanoramaSize(int size) {
            this.size = size;
        }

        public int size() {
            return size;
        }
    }

    public static final Path UNIFIED_FOLDER = switch (Util.getPlatform()) {
        case WINDOWS -> Path.of(System.getenv("APPDATA"), ".snapper");
        case OSX -> Path.of(SystemProperties.getUserHome(), "Library", "Application Support", "snapper");
        default -> Path.of(SystemProperties.getUserHome(), ".snapper");
    };
}
