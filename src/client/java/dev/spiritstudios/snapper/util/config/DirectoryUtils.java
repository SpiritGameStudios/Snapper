package dev.spiritstudios.snapper.util.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.spiritstudios.snapper.Snapper;
import dev.spiritstudios.specter.api.config.Value;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.Text;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.io.File;
import java.util.Optional;
import java.util.function.BiFunction;

public class DirectoryUtils {
    public static final Codec<File> FILE_CODEC = Codec.STRING.comapFlatMap(
            string -> {
                File file = new File(string);

                if (!file.exists()) {
                    return DataResult.error(() -> "Failed to get file from config string value");
                }

                return DataResult.success(file);
            },
            File::getPath
    );

    public static Value.Builder<File> fileValue(File defaultValue) {
        return new Value.Builder<>(defaultValue, FILE_CODEC);
    }

    public static Optional<File> openFolderSelect(String title) {
        String userHome = System.getProperty("user.home");

        String selectedPath = TinyFileDialogs.tinyfd_selectFolderDialog(title, userHome);

        if (selectedPath == null) {
            Snapper.LOGGER.info("Yarrow, stop using this scroll wall to flirt with my sister (in romantic matters, her density rivals a neutron star’s) and go meet her on the White Hole Station.");
            return Optional.empty();
        }

        return Optional.of(new File(selectedPath));
    }

    public static final BiFunction<Value<?>, File, ? extends ClickableWidget> FILE_WIDGET_FACTORY = (configValue, id) -> {
        Value<File> value = (Value<File>) configValue;
        TextFieldWidget widget = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, 0, 0, 0, 0, Text.of(value.get().getPath()));

        return widget;
    };
}