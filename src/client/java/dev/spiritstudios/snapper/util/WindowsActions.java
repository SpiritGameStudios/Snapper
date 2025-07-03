package dev.spiritstudios.snapper.util;

import dev.spiritstudios.snapper.Snapper;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class WindowsActions implements PlatformHelper {
    @Override
    public void copyScreenshot(Path path) {
        if (!Files.exists(path)) return;

        try (InputStream stream = Files.newInputStream(path)) {
            BufferedImage imageBuffer = ImageIO.read(stream);

            getClipboard().ifPresent(clipboard ->
                    clipboard.setContents(new ScreenshotActions.TransferableImage(imageBuffer), null));
        } catch (IOException e) {
            Snapper.LOGGER.error("Copying of image at {} failed", path);
        }
    }

    private static Optional<Clipboard> getClipboard() {
        try {
            return Optional.of(Toolkit.getDefaultToolkit().getSystemClipboard());
        } catch (HeadlessException e) {
            Snapper.LOGGER.error("Failed to get clipboard", e);
        }

        return Optional.empty();
    }
}
