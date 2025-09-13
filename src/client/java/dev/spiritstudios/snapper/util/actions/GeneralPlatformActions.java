package dev.spiritstudios.snapper.util.actions;

import dev.spiritstudios.snapper.Snapper;
import dev.spiritstudios.snapper.util.PlatformHelper;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class GeneralPlatformActions implements PlatformHelper {
    @Override
    public void copyScreenshot(Path path) {
        if (!Files.exists(path)) {
			Snapper.LOGGER.warn("Attempted to copy screenshot {} that does not exist", path);
			return;
		}

        try (InputStream stream = Files.newInputStream(path)) {
            BufferedImage imageBuffer = ImageIO.read(stream);

            getClipboard().ifPresent(clipboard ->
                    clipboard.setContents(new TransferableImage(imageBuffer), null));
        } catch (IOException e) {
            Snapper.LOGGER.error("Copying of image at {} failed", path);
        }
    }

    private static Optional<Clipboard> getClipboard() {
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
			return new DataFlavor[] {
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
