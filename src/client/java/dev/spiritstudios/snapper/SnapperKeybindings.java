package dev.spiritstudios.snapper;

import dev.spiritstudios.snapper.gui.screen.ScreenshotScreen;
import dev.spiritstudios.snapper.gui.screen.ScreenshotViewerScreen;
import dev.spiritstudios.snapper.gui.toast.SnapperToast;
import dev.spiritstudios.snapper.util.ScreenshotTexture;
import dev.spiritstudios.snapper.util.ScreenshotActions;
import dev.spiritstudios.specter.api.core.client.event.ClientKeybindEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Path;
import java.util.List;

public final class SnapperKeybindings {
    public static final KeyMapping.Category SNAPPER = KeyMapping.Category.register(Snapper.id("snapper"));

    public static final KeyMapping PANORAMA_KEY = new KeyMapping(
            "key.snapper.panorama",
            GLFW.GLFW_KEY_F8,
            SNAPPER
    );

    public static final KeyMapping RECENT_SCREENSHOT_KEY = new KeyMapping(
            "key.snapper.recent",
            GLFW.GLFW_KEY_O,
            SNAPPER
    );

    public static final KeyMapping SCREENSHOT_MENU_KEY = new KeyMapping(
            "key.snapper.screenshot_menu",
            GLFW.GLFW_KEY_V,
            SNAPPER
    );

    public static void init() {
        KeyBindingHelper.registerKeyBinding(PANORAMA_KEY);
        KeyBindingHelper.registerKeyBinding(RECENT_SCREENSHOT_KEY);
        KeyBindingHelper.registerKeyBinding(SCREENSHOT_MENU_KEY);

        ClientKeybindEvents.pressed(SCREENSHOT_MENU_KEY).register(client ->
                client.setScreen(new ScreenshotScreen(client.screen)));

        ClientKeybindEvents.pressed(PANORAMA_KEY).register(SnapperKeybindings::takePanorama);
        ClientKeybindEvents.pressed(RECENT_SCREENSHOT_KEY).register(SnapperKeybindings::openRecentScreenshot);
    }

    private static void takePanorama(Minecraft client) {
        if (client.player == null) return;
        client.grabPanoramixScreenshot(client.gameDirectory);

        SnapperToast.push(
                SnapperToast.Type.PANORAMA,
                Component.translatable("toast.snapper.panorama.created"),
                Component.translatable(
                        "toast.snapper.panorama.created.description",
                        SCREENSHOT_MENU_KEY.getTranslatedKeyMessage()
                )
        );
    }

    private static void openRecentScreenshot(Minecraft client) {
        List<Path> screenshots = ScreenshotActions.getScreenshots();
        if (screenshots.isEmpty()) {
            SnapperToast.push(
                    SnapperToast.Type.SCREENSHOT,
                    Component.translatable("toast.snapper.screenshot.recent.failure"),
                    Component.translatable("toast.snapper.screenshot.recent.failure.not_exist")
            );
            return;
        }

        Path latestPath = screenshots.getFirst();
        ScreenshotTexture.createScreenshot(client.getTextureManager(), latestPath)
                .ifPresentOrElse(
                        image -> {
                            client.setScreen(new ScreenshotViewerScreen(
                                    image,
                                    latestPath,
                                    client.screen
                            ));
                            image.load();
                        },
                        () -> SnapperToast.push(
                                SnapperToast.Type.DENY,
                                Component.translatable("toast.snapper.screenshot.recent.failure"),
                                Component.translatable("toast.snapper.screenshot.recent.failure.generic")
                        )
                );

    }
}
