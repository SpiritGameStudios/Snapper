package dev.spiritstudios.snapper.util.clipboard.linux;

import dev.spiritstudios.snapper.Snapper;
import dev.spiritstudios.snapper.util.clipboard.Clipboard;

import java.io.*;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class ProcessClipboard implements Clipboard {
    protected abstract List<String> command();

    @Override
    public boolean copyScreenshot(Path path) {
        try {
            Process process = new ProcessBuilder(this.command()).redirectInput(path.toFile()).start();

            try (
                    InputStream _ = process.getInputStream();
                    OutputStream _ = process.getOutputStream();
                    InputStream stderr = process.getErrorStream()
            ) {
                if (!process.waitFor(1, TimeUnit.SECONDS)) {
                    process.destroyForcibly();
                    Snapper.LOGGER.error("Failed to copy image at {}, timed out.", path);
                    return false;
                }

                if (process.exitValue() == 0) {
                    return true;
                } else {
                    try (
                            InputStreamReader isr = new InputStreamReader(stderr);
                            BufferedReader reader = new BufferedReader(isr);
                    ) {
                        StringBuilder stringBuilder = new StringBuilder();
                        while (reader.ready()) stringBuilder.append(reader.readLine()).append(System.lineSeparator());

                        Snapper.LOGGER.error("Failed to copy image at {}. STDERR is as follows: {}", path, stringBuilder);
                    }

                    return false;
                }
            }
        } catch (IOException | InterruptedException e) {
            Snapper.LOGGER.error("Failed to copy image at {}.", path, e);
            return false;
        }
    }
}
