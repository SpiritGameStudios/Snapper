package dev.spiritstudios.snapper.util;

import dev.spiritstudios.snapper.Snapper;
import net.minecraft.client.MinecraftClient;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class WindowsActions implements PlatformHelper {
    @Override
    public void copyScreenshot(File screenshot) {
        if (getClipboard() == null || !screenshot.exists()) return;

        try {
            BufferedImage imageBuffer = ImageIO.read(screenshot);

            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(new ScreenshotActions.TransferableImage(imageBuffer), null);
        } catch (IOException e) {
            Snapper.LOGGER.error("Copying of image at {} failed", screenshot.toPath());
        }
    }

    private static Clipboard getClipboard() {
        try {
            return Toolkit.getDefaultToolkit().getSystemClipboard();
        } catch (HeadlessException e) {
            Snapper.LOGGER.error("Failed to get clipboard", e);
        }

        return null;
    }
}
