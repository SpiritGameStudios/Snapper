package dev.spiritstudios.snapper;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SnapperCodecs {
    public static final Codec<Path> PATH = Codec.STRING.comapFlatMap(
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
            path -> path.toString().replace("\\", "\\\\")
    );

    public static final Codec<Integer> POSITIVE_POWER_OF_2 = ExtraCodecs.POSITIVE_INT.validate(SnapperCodecs::validatePowerOf2);

    public static DataResult<Integer> validatePowerOf2(int value) {
        return Mth.isPowerOfTwo(value) ?
                DataResult.success(value) :
                DataResult.error(() -> "Value " + value + " is not a power of 2.");
    }
}
