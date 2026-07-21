package dev.spiritstudios.snapper.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import joptsimple.internal.Strings;
import org.apache.commons.lang3.SystemProperties;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class DirectoryConfigUtil {

    public static CompletableFuture<Optional<Path>> openFolderSelect(String title) {
        // replaceAll is to prevent an ACE exploit in TinyFD
        return CompletableFuture.supplyAsync(
                        () -> TinyFileDialogs.tinyfd_selectFolderDialog(
                                title.replaceAll("[^a-zA-Z0-9 .,]", ""),
                                SystemProperties.getUserHome()
                        )
                )
                .thenApply(selectedPath -> {
                    if (Strings.isNullOrEmpty(selectedPath)) {
                        return Optional.empty();
                    }

                    return Optional.of(Path.of(selectedPath));
                });
    }
}
