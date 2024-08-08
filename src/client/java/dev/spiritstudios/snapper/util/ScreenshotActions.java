package dev.spiritstudios.snapper.util;

import dev.spiritstudios.snapper.Snapper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.ProgressScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ScreenshotActions {
    private static final Clipboard clipboard = getClipboard();

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void deleteScreenshot(File screenshot, Screen screen) {
        if (!screenshot.exists()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        client.setScreen(
                new ConfirmScreen(
                        confirmed -> {
                            if (confirmed) {
                                client.setScreen(new ProgressScreen(true));
                                screenshot.delete();
                            }
                            client.setScreen(screen);
                        },
                        Text.translatable("text.snapper.delete_question"),
                        Text.translatable("text.snapper.delete_warning", screenshot.getName()),
                        Text.translatable("button.snapper.delete"),
                        ScreenTexts.CANCEL
                )
        );
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void renameScreenshot(File screenshot, String newName) {
        if (screenshot.exists()) {
            screenshot.renameTo(new File(screenshot.getParentFile().toPath() + "/" + newName));
        }
    }

    @Nullable
    private static Clipboard getClipboard() {
        if (MinecraftClient.IS_SYSTEM_MAC) return null;

        try {
            return Toolkit.getDefaultToolkit().getSystemClipboard();
        } catch (HeadlessException e) {
            Snapper.LOGGER.error("Failed to get clipboard", e);
        }

        return null;
    }

    public static void copyScreenshot(File screenshot) {
        if (MinecraftClient.IS_SYSTEM_MAC) {
            ScreenshotActionsMac.copyScreenshotMac(screenshot.getAbsolutePath());
            return;
        }

        if (clipboard != null && screenshot.exists()) {
            try {
                BufferedImage imageBuffer = ImageIO.read(screenshot);

                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(new TransferableImage(imageBuffer), null);
            } catch (IOException e) {
                Snapper.LOGGER.error(String.format("Copying of image at %s failed", screenshot.toPath()));
            }
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
