package dev.spiritstudios.snapper.util;

import dev.spiritstudios.snapper.Snapper;
import dev.spiritstudios.snapper.SnapperConfig;
import dev.spiritstudios.snapper.gui.screen.ReloadableScreen;
import dev.spiritstudios.snapper.render.texture.GalleryTexture;
import dev.spiritstudios.snapper.render.texture.PanoramaTexture;
import dev.spiritstudios.snapper.render.texture.ScreenshotTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public final class ScreenshotActions {
    public static void deleteScreenshot(Path path, Screen screen) {
        if (!Files.exists(path)) return;

        Minecraft minecraft = Minecraft.getInstance();
        minecraft.gui.setScreen(
                new ConfirmScreen(
                        confirmed -> {
                            if (confirmed) {
                                minecraft.gui.setScreen(new ProgressScreen(true));
                                try {
                                    Files.deleteIfExists(path);
                                } catch (IOException e) {
                                    Snapper.LOGGER.error("Failed to delete file", e);
                                }

                                if (screen instanceof ReloadableScreen reloadable) {
                                    reloadable.reload();
                                }
                            }

                            minecraft.gui.setScreen(screen);
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

    private static final Comparator<Path> SCREENSHOT_SORTER = Comparator.<Path>comparingLong(
            path ->
                    SafeFiles.getLastModifiedTime(path)
                            .map(FileTime::toMillis)
                            .orElse(0L)
    ).reversed();

    public static Path getScreenshotDirectory() {
        if (SnapperConfig.HOLDER.get().customScreenshotPath().enabled()) {
            Path customPath = SnapperConfig.HOLDER.get().customScreenshotPath().path().resolve("screenshots");

            if (!SafeFiles.createDirectories(customPath)) {
                Snapper.LOGGER.error("Failed to create directories of configured custom screenshot folder");
            }

            return customPath;
        }

        return Minecraft.getInstance().gameDirectory.toPath().resolve("screenshots");
    }

    public static Path getPanoramaDirectory() {
        return getScreenshotDirectory().resolve("panoramas");
    }

    private static Stream<Path> listScreenshots(Path directory) throws IOException {
        return Files.list(directory)
                .filter(path -> !Files.isDirectory(path) && SafeFiles.isContentType(path, "image/png", ".png"))
                .sorted(SCREENSHOT_SORTER);
    }

    public static List<Path> getScreenshots() {
        Path screenshotDirectory = ScreenshotActions.getScreenshotDirectory();
        if (Files.notExists(screenshotDirectory)) return List.of();

        try (Stream<Path> paths = listScreenshots(screenshotDirectory)) {
            return paths.toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<GalleryTexture> getScreenshotTextures(TextureManager textureManager) {
        Path screenshotDirectory = ScreenshotActions.getScreenshotDirectory();
        if (Files.notExists(screenshotDirectory)) return List.of();

        try (Stream<Path> paths = listScreenshots(screenshotDirectory)) {
            return paths
                    .<GalleryTexture>flatMap(path -> ScreenshotTexture.createScreenshot(textureManager, path).stream())
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<GalleryTexture> getPanoramaTextures(TextureManager textureManager) {
        Path screenshotDirectory = ScreenshotActions.getPanoramaDirectory();
        if (Files.notExists(screenshotDirectory)) return List.of();

        try (Stream<Path> paths = listScreenshots(screenshotDirectory)) {
            return paths
                    .<GalleryTexture>flatMap(path -> PanoramaTexture.createScreenshot(textureManager, path).stream())
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
