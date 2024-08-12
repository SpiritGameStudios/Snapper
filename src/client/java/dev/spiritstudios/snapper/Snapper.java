package dev.spiritstudios.snapper;

import dev.spiritstudios.snapper.gui.ScreenshotScreen;
import dev.spiritstudios.snapper.gui.ScreenshotViewerScreen;
import dev.spiritstudios.snapper.util.ScreenshotIcon;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class Snapper implements ClientModInitializer {
    private final MinecraftClient client = MinecraftClient.getInstance();
    public static final String MODID = "snapper";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    private static final KeyBinding SCREENSHOT_MENU_KEY = KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                    "key.snapper.screenshot_menu",
                    GLFW.GLFW_KEY_V,
                    "key.categories.misc"
            ));

    private static final KeyBinding RECENT_SCREENSHOT_KEY = KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                    "key.snapper.recent",
                    GLFW.GLFW_KEY_O,
                    "key.categories.misc"
            ));


    private static final KeyBinding PANORAMA_KEY = KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                    "key.snapper.panorama",
                    GLFW.GLFW_KEY_F8,
                    "key.categories.misc"
            ));

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (SCREENSHOT_MENU_KEY.wasPressed()) client.setScreen(new ScreenshotScreen(null));
            while (PANORAMA_KEY.wasPressed()) {
                if (client.player == null) continue;
                client.player.sendMessage(client.takePanorama(client.runDirectory, 1024, 1024), true);
            }
            while (RECENT_SCREENSHOT_KEY.wasPressed()) {
                client.setScreen(new ScreenshotViewerScreen(
                        ScreenshotIcon.of(getLatestScreenshot()),
                        getLatestScreenshot(),
                        null
                ));
            }
        });
    }

    private File getLatestScreenshot() {
        File screenshotDir = new File(client.runDirectory, "screenshots");

        File[] files = screenshotDir.listFiles();
        List<File> screenshots = new ArrayList<>(List.of(files == null ? new File[0] : files));

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

        return screenshots.getFirst();
    }
}