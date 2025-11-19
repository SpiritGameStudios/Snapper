package dev.spiritstudios.snapper;

import dev.spiritstudios.snapper.gui.screen.ScreenshotScreen;
import dev.spiritstudios.snapper.gui.screen.ScreenshotViewerScreen;
import dev.spiritstudios.snapper.gui.toast.SnapperToast;
import dev.spiritstudios.snapper.util.DynamicTexture;
import dev.spiritstudios.snapper.util.ScreenshotActions;
import dev.spiritstudios.specter.api.core.client.event.ClientKeybindEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Path;
import java.util.List;

public final class SnapperKeybindings {
    public static final KeyBinding.Category SNAPPER = KeyBinding.Category.create(Snapper.id("snapper"));

    public static final KeyBinding PANORAMA_KEY = new KeyBinding(
            "key.snapper.panorama",
            GLFW.GLFW_KEY_F8,
            SNAPPER
    );

    public static final KeyBinding RECENT_SCREENSHOT_KEY = new KeyBinding(
            "key.snapper.recent",
            GLFW.GLFW_KEY_O,
            SNAPPER
    );

    public static final KeyBinding SCREENSHOT_MENU_KEY = new KeyBinding(
            "key.snapper.screenshot_menu",
            GLFW.GLFW_KEY_V,
            SNAPPER
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
        client.takePanorama(client.runDirectory);

        SnapperToast.push(
                SnapperToast.Type.PANORAMA,
                Text.translatable("toast.snapper.panorama.created"),
                Text.translatable(
                        "toast.snapper.panorama.created.description",
                        SCREENSHOT_MENU_KEY.getBoundKeyLocalizedText()
                )
        );
    }

    private static void openRecentScreenshot(MinecraftClient client) {
        List<Path> screenshots = ScreenshotActions.getScreenshots();
        if (screenshots.isEmpty()) {
            SnapperToast.push(
                    SnapperToast.Type.SCREENSHOT,
                    Text.translatable("toast.snapper.screenshot.recent.failure"),
                    Text.translatable("toast.snapper.screenshot.recent.failure.not_exist")
            );
            return;
        }

        Path latestPath = screenshots.getFirst();
        DynamicTexture.createScreenshot(client.getTextureManager(), latestPath)
                .ifPresentOrElse(
                        image -> {
                            client.setScreen(new ScreenshotViewerScreen(
                                    image,
                                    latestPath,
                                    client.currentScreen
                            ));
                            image.load();
                        },
                        () -> SnapperToast.push(
                                SnapperToast.Type.DENY,
                                Text.translatable("toast.snapper.screenshot.recent.failure"),
                                Text.translatable("toast.snapper.screenshot.recent.failure.generic")
                        )
                );

    }
}
