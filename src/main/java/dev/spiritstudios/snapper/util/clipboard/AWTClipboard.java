package dev.spiritstudios.snapper.util.clipboard;

import dev.spiritstudios.snapper.Snapper;
import org.apache.commons.lang3.SystemProperties;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.regex.Pattern;

public class AWTClipboard implements Clipboard {
    private static final Pattern POTENTIALLY_INCOMPATIBLE_JVM_PATTERN = Pattern.compile("java-runtime-(alpha|beta|delta|epsilon|gamma)");

    public AWTClipboard() {
        if (POTENTIALLY_INCOMPATIBLE_JVM_PATTERN.matcher(SystemProperties.getJavaHome()).find() ||
                "https://aka.ms/minecraftjavacrashes".equals(System.getProperty("java.vendor.url.bug", ""))) {
            Snapper.LOGGER.warn("You appear to be using the Minecraft Launcher's default Java Virtual Machine (JVM). This is known to cause issues with the Auto-clipboard feature. Please try switching to another JVM before reporting any bugs related to the clipboard.");
        }
    }

    @Override
    public boolean copyScreenshot(Path path) {
        if (!Files.exists(path)) {
            Snapper.LOGGER.warn("Attempted to copy screenshot {} that does not exist", path);
            return false;
        }

        try (InputStream stream = Files.newInputStream(path)) {
            BufferedImage imageBuffer = ImageIO.read(stream);

            getClipboard().ifPresent(clipboard ->
                    clipboard.setContents(new TransferableImage(imageBuffer), null));

            return true;
        } catch (NoClassDefFoundError | AWTError e) {
            Snapper.LOGGER.error("Failed to copy image at {}. This is likely because your Java Virtual Machine does not properly support AWT. Please try switching to another JVM before reporting this as a bug.", path, e);
            return false;
        } catch (Throwable e) {
            Snapper.LOGGER.error("Failed to copy image at {}.", path, e);
            return false;
        }
    }

    private static Optional<java.awt.datatransfer.Clipboard> getClipboard() {
        try {
            return Optional.of(Toolkit.getDefaultToolkit().getSystemClipboard());
        } catch (HeadlessException e) {
            Snapper.LOGGER.error("Failed to get clipboard", e);
            return Optional.empty();
        }
    }

    record TransferableImage(Image image) implements Transferable {
        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{
                    DataFlavor.imageFlavor
            };
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return DataFlavor.imageFlavor.equals(flavor);
        }

        @NotNull
        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (!isDataFlavorSupported(flavor)) throw new UnsupportedFlavorException(flavor);

            return image();
        }
    }
}
