package dev.spiritstudios.snapper.util.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.spiritstudios.snapper.gui.widget.FolderSelectWidget;
import dev.spiritstudios.specter.api.config.Value;
import joptsimple.internal.Strings;
import net.minecraft.client.gui.widget.ClickableWidget;
import org.apache.commons.lang3.SystemProperties;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.BiFunction;

public class DirectoryConfigUtil {
    public static final Codec<Path> PATH_CODEC = Codec.STRING.comapFlatMap(
            string -> {
                Path path = Path.of(string);

                try {
                    Files.createDirectories(path);
                } catch (IOException e) {
                    return DataResult.error(e::getMessage);
                }

                if (!Files.exists(path)) {
                    return DataResult.error(() -> "Failed to get file from config string value. Does the directory exist?");
                }

                return DataResult.success(path);
            },
            path -> escapePath(path.toString())
    );

    public static Optional<Path> openFolderSelect(String title) {
        String selectedPath = TinyFileDialogs.tinyfd_selectFolderDialog(title, SystemProperties.getUserHome());

        if (Strings.isNullOrEmpty(selectedPath)) {
            return Optional.empty();
        }

        return Optional.of(Path.of(selectedPath));
    }

    public static final BiFunction<Value<?>, String, ? extends ClickableWidget> PATH_WIDGET_FACTORY = (configValue, id) -> {
        @SuppressWarnings("unchecked") Value<Path> value = (Value<Path>) configValue;

        return new FolderSelectWidget(0, 0, 10, 10, value, "%s.placeholder".formatted(configValue.translationKey(id)));
    };

    public static String escapePath(String path) {
        return path.replace("\\", "\\\\");
    }
}