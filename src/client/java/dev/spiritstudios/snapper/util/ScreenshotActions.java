package dev.spiritstudios.snapper.util;

import dev.spiritstudios.snapper.Snapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class ScreenshotActions {
    public static void deleteScreenshot(Path path, Screen screen) {
        if (!Files.exists(path)) return;

        Minecraft client = Minecraft.getInstance();
        client.setScreen(
                new ConfirmScreen(
                        confirmed -> {
                            if (confirmed) {
                                client.setScreen(new ProgressScreen(true));
                                try {
                                    Files.deleteIfExists(path);
                                } catch (IOException e) {
                                    Snapper.LOGGER.error("Failed to delete file", e);
                                }
                            }
                            client.setScreen(screen);
                        },
                        Component.translatable("text.snapper.delete_question"),
                        Component.translatable("text.snapper.delete_warning", path.getFileName()),
                        Component.translatable("button.snapper.delete"),
                        CommonComponents.GUI_CANCEL
                )
        );
    }

    public static void renameScreenshot(Path screenshot, String newName) {
        if (Files.exists(screenshot)) {
            try {
                Files.move(screenshot, screenshot.getParent().resolve(newName));
            } catch (IOException e) {
                Snapper.LOGGER.error("Failed to rename file", e);
            }
        }
    }

    public static List<Path> getScreenshots() {
        Path screenshotDirectory = SnapperUtil.getConfiguredScreenshotDirectory();
        if (!Files.exists(screenshotDirectory)) {
            return List.of();
        }
        try (Stream<Path> stream = Files.list(screenshotDirectory)) {
            return stream.filter(path ->
                            !Files.isDirectory(path) && SafeFiles.isContentType(path, "image/png", ".png"))
                    .sorted(Comparator.<Path>comparingLong(path ->
                                    SafeFiles.getLastModifiedTime(path)
                                            .map(FileTime::toMillis)
                                            .orElse(0L))
                            .reversed())
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
