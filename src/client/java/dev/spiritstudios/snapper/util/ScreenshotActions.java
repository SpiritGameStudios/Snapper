package dev.spiritstudios.snapper.util;

import dev.spiritstudios.snapper.Snapper;
import dev.spiritstudios.snapper.SnapperConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.ProgressScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class ScreenshotActions {
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

    public static java.util.List<File> getScreenshots(MinecraftClient client) {
        File customScreenshotDirectory = new File(SnapperConfig.INSTANCE.customScreenshotFolder.get(), "screenshots");
        File defaultScreenshotDirectory = new File(client.runDirectory, "screenshots");
        File screenshotDir = SnapperConfig.INSTANCE.useCustomScreenshotFolder.get() ? customScreenshotDirectory : defaultScreenshotDirectory;

        File[] files = screenshotDir.listFiles();
        java.util.List<File> screenshots = new ArrayList<>(List.of(files == null ? new File[0] : files));

        screenshots.removeIf(file -> {
            if (Files.isDirectory(file.toPath())) return true;
            String fileType;

            try {
                fileType = Files.probeContentType(file.toPath());
            } catch (IOException e) {
                Snapper.LOGGER.error("Couldn't load screenshot list", e);
                return true;
            }

            return !Objects.equals(fileType, "image/png");
        });

        screenshots.sort(Comparator.comparingLong(File::lastModified).reversed());
        return screenshots;
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
