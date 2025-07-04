package dev.spiritstudios.snapper.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
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

    public static Optional<FileTime> getLastModifiedTime(Path path) {
        try {
            return Optional.of(Files.getLastModifiedTime(path));
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
