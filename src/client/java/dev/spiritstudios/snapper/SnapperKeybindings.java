package dev.spiritstudios.snapper;

import dev.spiritstudios.snapper.gui.screen.ScreenshotScreen;
import dev.spiritstudios.snapper.gui.screen.ScreenshotViewerScreen;
import dev.spiritstudios.snapper.util.ScreenshotActions;
import dev.spiritstudios.snapper.util.ScreenshotImage;
import dev.spiritstudios.specter.api.core.client.event.ClientKeybindEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Path;
import java.util.List;

public final class SnapperKeybindings {
    public static final KeyBinding PANORAMA_KEY = new KeyBinding(
            "key.snapper.panorama",
            GLFW.GLFW_KEY_F8,
            "key.categories.snapper"
    );

    public static final KeyBinding RECENT_SCREENSHOT_KEY = new KeyBinding(
            "key.snapper.recent",
            GLFW.GLFW_KEY_O,
            "key.categories.snapper"
    );

    public static final KeyBinding SCREENSHOT_MENU_KEY = new KeyBinding(
            "key.snapper.screenshot_menu",
            GLFW.GLFW_KEY_V,
            "key.categories.snapper"
    );

    public static void init() {
        KeyBindingHelper.registerKeyBinding(PANORAMA_KEY);
        KeyBindingHelper.registerKeyBinding(RECENT_SCREENSHOT_KEY);
        KeyBindingHelper.registerKeyBinding(SCREENSHOT_MENU_KEY);

        ClientKeybindEvents.pressed(SCREENSHOT_MENU_KEY).register(client ->
                client.setScreen(new ScreenshotScreen(client.currentScreen)));

        ClientKeybindEvents.pressed(PANORAMA_KEY).register(SnapperKeybindings::takePanorama);
        ClientKeybindEvents.pressed(RECENT_SCREENSHOT_KEY).register(SnapperKeybindings::openRecentScreenshot);
    }

    private static void takePanorama(MinecraftClient client) {
        if (client.player == null) return;

        if (Snapper.IS_IRIS_INSTALLED) {
            client.player.sendMessage(Text.translatable("text.snapper.panorama_failure_iris"), true);
            return;
        }

        client.takePanorama(client.runDirectory, 1024, 1024);
        client.player.sendMessage(Text.translatable(
                "text.snapper.panorama_success",
                SCREENSHOT_MENU_KEY.getBoundKeyLocalizedText()
        ), true);
    }

    private static void openRecentScreenshot(MinecraftClient client) {
        List<Path> screenshots = ScreenshotActions.getScreenshots();
        if (screenshots.isEmpty()) {
            if (client.player != null)
                client.player.sendMessage(Text.translatable("text.snapper.screenshot_not_exists"), true);
            return;
        }

        Path latestPath = screenshots.getFirst();
        ScreenshotImage.createScreenshot(client.getTextureManager(), latestPath)
                .ifPresentOrElse(
                        image -> client.setScreen(new ScreenshotViewerScreen(
                                image,
                                latestPath,
                                client.currentScreen
                        )),
                        () -> {
                            if (client.player != null)
                                client.player.sendMessage(Text.translatable("text.snapper.screenshot_open_failure"), true);
                        }
                );

    }
}
