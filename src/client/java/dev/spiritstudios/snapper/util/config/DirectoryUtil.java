package dev.spiritstudios.snapper.util.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.spiritstudios.snapper.gui.widget.FolderSelectWidget;
import dev.spiritstudios.specter.api.config.Value;
import net.minecraft.client.gui.widget.ClickableWidget;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;
import java.util.function.BiFunction;

public class DirectoryUtil {
    public static final Codec<File> FILE_CODEC = Codec.STRING.comapFlatMap(
            string -> {
                File file = new File(string);

                try {
                    Files.createDirectories(file.toPath());
                } catch (IOException e) {
                    return DataResult.error(() -> "Yarrow, stop using this scroll wall to flirt with my sister (in romantic matters, her density rivals a neutron star’s) and go meet her on the White Hole Station.");
                }

                if (!file.exists()) {
                    return DataResult.error(() -> "Failed to get file from config string value. Does the directory exist?");
                }

                return DataResult.success(file);
            },
            file -> escapePath(file.getPath())
    );

    public static Value.Builder<File> fileValue(File defaultValue) {
        return new Value.Builder<>(defaultValue, FILE_CODEC);
    }

    public static Optional<File> openFolderSelect(String title) {
        String userHome = System.getProperty("user.home");

        String selectedPath = TinyFileDialogs.tinyfd_selectFolderDialog(title, userHome);

        if (selectedPath == null) {
            return Optional.empty();
        }

        return Optional.of(new File(selectedPath));
    }

    public static final BiFunction<Value<?>, String, ? extends ClickableWidget> FILE_WIDGET_FACTORY = (configValue, id) -> {
        Value<File> value = (Value<File>) configValue;

        /*
        TextFieldWidget widget = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, 0, 0, 0, 0, Text.of(value.get().getPath()));
        widget.setPlaceholder(Text.translatableWithFallback("%s.placeholder".formatted(configValue.translationKey(id)), "").formatted(Formatting.DARK_GRAY));
        widget.setMaxLength(Integer.MAX_VALUE);
        widget.setText(value.get().getPath());
        widget.setChangedListener(content -> value.set(new File(content)));
*/
        return new FolderSelectWidget(0, 0, 10, 10, value, "%s.placeholder".formatted(configValue.translationKey(id)));
    };

    public static String escapePath(String path) {
        return path.replace("\\", "\\\\").trim();
    }
}