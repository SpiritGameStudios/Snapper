package dev.spiritstudios.snapper.util;

import dev.spiritstudios.snapper.Snapper;
import org.jetbrains.annotations.Nullable;

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

    public static boolean isContentType(Path path, String matchType, String extension) {
        if (!Files.exists(path)) return false;
        try {
            @Nullable String foundType = Files.probeContentType(path);
            if (foundType != null) {
                return foundType.equals(matchType);
            }
        } catch (IOException | NullPointerException e) {
            Snapper.LOGGER.error("Failed to get MIME type of file name {}; attempting extension match", path.getFileName());
            return false;
        }
        return path.getFileName().toString().endsWith(extension);
    }

    public static Optional<FileTime> getLastModifiedTime(Path path) {
        try {
            return Optional.of(Files.getLastModifiedTime(path));
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
