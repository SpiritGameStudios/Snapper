package dev.spiritstudios.snapper.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Util;
import org.apache.commons.lang3.SystemProperties;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;

public final class SnapperUtil {
    // Helper things. Please order alphabetically. <3 Lynn

    public static boolean inBoundingBox(int x, int y, int w, int h, double mouseX, double mouseY) {
        return mouseX > x && mouseX < x + w && mouseY > y && mouseY < y + h;
    }

    public static boolean isOfflineAccount() {
        return Minecraft.getInstance().getUser().getAccessToken().length() < 400;
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

    private static Path getAppDataFolder() {
        return switch (Util.getPlatform()) {
            case WINDOWS -> Path.of(System.getenv("APPDATA"));
            case OSX -> Path.of(SystemProperties.getUserHome(), "Library", "Application Support");
            default -> {
                String xdgDataHome = System.getenv("XDG_DATA_HOME");
                if (xdgDataHome != null) yield Path.of(xdgDataHome);
                else yield Path.of(SystemProperties.getUserHome(), ".local", "share");
            }
        };
    }

    public static final Path UNIFIED_FOLDER = getAppDataFolder().resolve("snapper");
}
