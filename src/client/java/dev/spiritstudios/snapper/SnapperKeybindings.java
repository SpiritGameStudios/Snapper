package dev.spiritstudios.snapper;

import dev.spiritstudios.snapper.gui.screen.ScreenshotScreen;
import dev.spiritstudios.snapper.gui.screen.ScreenshotViewerScreen;
import dev.spiritstudios.snapper.util.ScreenshotActions;
import dev.spiritstudios.snapper.util.ScreenshotImage;
import dev.spiritstudios.specter.api.core.util.ClientKeybindEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.util.List;

public final class SnapperKeybindings {
    public static final KeyBinding PANORAMA_KEY = new KeyBinding(
            "key.snapper.panorama",
            GLFW.GLFW_KEY_F8,
            "key.categories.misc"
    );

    public static final KeyBinding RECENT_SCREENSHOT_KEY = new KeyBinding(
            "key.snapper.recent",
            GLFW.GLFW_KEY_O,
            "key.categories.misc"
    );

    public static final KeyBinding SCREENSHOT_MENU_KEY = new KeyBinding(
            "key.snapper.screenshot_menu",
            GLFW.GLFW_KEY_V,
            "key.categories.misc"
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
        List<File> screenshots = ScreenshotActions.getScreenshots(client);
        if (screenshots.isEmpty()) {
            if (client.player != null)
                client.player.sendMessage(Text.translatable("text.snapper.screenshot_failure_open"), true);
            return;
        }

        File latestScreenshot = screenshots.getFirst();
        client.setScreen(new ScreenshotViewerScreen(
                ScreenshotImage.of(latestScreenshot, client.getTextureManager()),
                latestScreenshot,
                client.currentScreen
        ));
    }
}
