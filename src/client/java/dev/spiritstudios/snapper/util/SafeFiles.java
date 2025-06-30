package dev.spiritstudios.snapper.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public final class SafeFiles {
    public static boolean createDirectories(Path path) {
        try {
            Files.createDirectories(path);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static Optional<String> probeContentType(Path path) {
        try {
            return Optional.of(Files.probeContentType(path));
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
