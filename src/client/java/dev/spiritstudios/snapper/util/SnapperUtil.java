package dev.spiritstudios.snapper.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.spiritstudios.snapper.Snapper;
import dev.spiritstudios.snapper.SnapperConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Util;
import org.apache.commons.lang3.SystemProperties;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;

public final class SnapperUtil {
    // Helper things. Please order alphabetically. <3 Lynn

    public static boolean inBoundingBox(int x, int y, int w, int h, double mouseX, double mouseY) {
        return mouseX > x && mouseX < x + w && mouseY > y && mouseY < y + h;
    }

    public static FormattedCharSequence clipText(Font font, Component message, int width) {
        if (font.width(message) < width) return message.getVisualOrderText();

        FormattedText formattedText = font.substrByWidth(message, width - font.width(CommonComponents.ELLIPSIS));
        return Language.getInstance().getVisualOrder(FormattedText.composite(formattedText, CommonComponents.ELLIPSIS));
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
