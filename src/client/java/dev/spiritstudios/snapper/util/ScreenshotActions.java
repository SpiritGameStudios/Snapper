package dev.spiritstudios.snapper.util;

import dev.spiritstudios.snapper.Snapper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.ProgressScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

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

		MinecraftClient client = MinecraftClient.getInstance();
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
						Text.translatable("text.snapper.delete_question"),
						Text.translatable("text.snapper.delete_warning", path.getFileName()),
						Text.translatable("button.snapper.delete"),
						ScreenTexts.CANCEL
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
		try (Stream<Path> stream = Files.list(SnapperUtil.getConfiguredScreenshotDirectory())) {
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
