package dev.spiritstudios.snapper.util.config;

import joptsimple.internal.Strings;
import org.apache.commons.lang3.SystemProperties;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class DirectoryConfigUtil {

    public static CompletableFuture<Optional<Path>> openFolderSelect(String title) {
        return CompletableFuture.supplyAsync(() -> TinyFileDialogs.tinyfd_selectFolderDialog(title, SystemProperties.getUserHome()))
                .thenApply(selectedPath -> {
                    if (Strings.isNullOrEmpty(selectedPath)) {
                        return Optional.empty();
                    }

                    return Optional.of(Path.of(selectedPath));
                });
    }
}