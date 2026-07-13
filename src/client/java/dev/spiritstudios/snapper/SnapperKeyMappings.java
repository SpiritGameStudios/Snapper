package dev.spiritstudios.snapper;

import dev.spiritstudios.snapper.gui.screen.GalleryScreen;
import dev.spiritstudios.snapper.gui.screen.ScreenshotViewerScreen;
import dev.spiritstudios.snapper.gui.toast.SnapperToast;
import dev.spiritstudios.snapper.render.panorama.PanoramaGrabber;
import dev.spiritstudios.snapper.render.texture.ScreenshotTexture;
import dev.spiritstudios.snapper.util.ScreenshotActions;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Path;
import java.util.List;

public final class SnapperKeyMappings {
    public static final KeyMapping.Category SNAPPER_CATEGORY = KeyMapping.Category.register(Snapper.id(Snapper.MOD_ID));

    public static final KeyMapping PANORAMA_KEY = new KeyMapping(
            "key.snapper.panorama",
            GLFW.GLFW_KEY_F8,
            SNAPPER_CATEGORY
    );

    public static final KeyMapping RECENT_SCREENSHOT_KEY = new KeyMapping(
            "key.snapper.recent",
            GLFW.GLFW_KEY_B,
            SNAPPER_CATEGORY
    );

    public static final KeyMapping SCREENSHOT_MENU_KEY = new KeyMapping(
            "key.snapper.screenshot_menu",
            GLFW.GLFW_KEY_V,
            SNAPPER_CATEGORY
    );

    public static void init() {
        KeyMappingHelper.registerKeyMapping(PANORAMA_KEY);
        KeyMappingHelper.registerKeyMapping(RECENT_SCREENSHOT_KEY);
        KeyMappingHelper.registerKeyMapping(SCREENSHOT_MENU_KEY);

        ClientTickEvents.END_CLIENT_TICK.register(minecraft -> {
            while (PANORAMA_KEY.consumeClick()) SnapperKeyMappings.takePanorama(minecraft);
            while (RECENT_SCREENSHOT_KEY.consumeClick()) SnapperKeyMappings.openRecentScreenshot(minecraft);
            while (SCREENSHOT_MENU_KEY.consumeClick()) minecraft.gui.setScreen(new GalleryScreen(minecraft.gui.screen()));
        });
    }

    private static void takePanorama(Minecraft minecraft) {
        if (minecraft.player == null) return;
        PanoramaGrabber.grabSnapperPanorama(minecraft);

        SnapperToast.push(
                SnapperToast.Type.PANORAMA,
                Component.translatable("toast.snapper.panorama.created"),
                Component.translatable(
                        "toast.snapper.panorama.created.description",
                        SCREENSHOT_MENU_KEY.getTranslatedKeyMessage()
                )
        );
    }

    private static void openRecentScreenshot(Minecraft minecraft) {
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
        ScreenshotTexture.createScreenshot(minecraft.getTextureManager(), latestPath)
                .ifPresentOrElse(
                        image -> {
                            minecraft.gui.setScreen(new ScreenshotViewerScreen(
                                    image,
                                    minecraft.gui.screen()
                            ));
                        },
                        () -> SnapperToast.push(
                                SnapperToast.Type.DENY,
                                Component.translatable("toast.snapper.screenshot.recent.failure"),
                                Component.translatable("toast.snapper.screenshot.recent.failure.generic")
                        )
                );
    }
}
