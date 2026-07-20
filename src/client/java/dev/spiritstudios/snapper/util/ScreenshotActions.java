package dev.spiritstudios.snapper.util;

import dev.spiritstudios.snapper.Snapper;
import dev.spiritstudios.snapper.SnapperConfig;
import dev.spiritstudios.snapper.gui.screen.ReloadableScreen;
import dev.spiritstudios.snapper.gui.toast.SnapperToasts;
import dev.spiritstudios.snapper.render.texture.GalleryTexture;
import dev.spiritstudios.snapper.render.texture.PanoramaTexture;
import dev.spiritstudios.snapper.render.texture.ScreenshotTexture;
import dev.spiritstudios.snapper.util.clipboard.Clipboard;
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

    public static void copyScreenshot(Path path, boolean successToast) {
        if (Clipboard.INSTANCE.copyScreenshot(path)) {
            if (successToast) {
                SnapperToasts.screenshotCopySuccess();
            }
        } else {
            SnapperToasts.screenshotCopyFailure();
        }
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

    public static Path getSnapperDataDir() {
        Path path = SnapperConfig.get().customScreenshotPath().enabled() ?
                SnapperConfig.get().customScreenshotPath().path() :
                Minecraft.getInstance().gameDirectory.toPath();

        if (!SafeFiles.createDirectories(path)) {
            Snapper.LOGGER.error("Failed to create snapper directory");
        }

        return path;
    }

    public static Path getScreenshotDirectory() {
        Path path = getSnapperDataDir().resolve("screenshots");

        if (!SafeFiles.createDirectories(path)) {
            Snapper.LOGGER.error("Failed to create screenshot directory");
        }

        return path;
    }

    public static Path getPanoramaDirectory() {
        Path path = getSnapperDataDir().resolve("panoramas");
        if (!SafeFiles.createDirectories(path)) Snapper.LOGGER.error("Failed to create panorama directory");
        return path;
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
                    .<GalleryTexture>map(path -> ScreenshotTexture.createScreenshot(textureManager, path))
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
                    .<GalleryTexture>map(path -> PanoramaTexture.createScreenshot(textureManager, path))
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
