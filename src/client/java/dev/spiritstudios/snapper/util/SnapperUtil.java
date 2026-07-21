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
