package dev.spiritstudios.snapper.util.clipboard.linux;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public final class PathTraverser {
    public static boolean isOnPath(String name) {
        String path = System.getenv("PATH");
        if (path == null || path.isEmpty()) return false;

        for (String dir : path.split(":")) {
            Path programPath = Path.of(dir).resolve(name);
            if (Files.exists(programPath) && Files.isExecutable(programPath)) {
                return true;
            }
        }

        return false;
    }
}
