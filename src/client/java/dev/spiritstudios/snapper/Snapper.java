package dev.spiritstudios.snapper;

import dev.spiritstudios.snapper.gui.ScreenshotScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Snapper implements ClientModInitializer {
    public static final String MODID = "snapper";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    private static final KeyBinding SCREENSHOT_MENU_KEY = KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                    "key.snapper.screenshot_menu",
                    GLFW.GLFW_KEY_M,
                    "key.categories.misc"
            ));


    private static final KeyBinding PANORAMA_KEY = KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                    "key.snapper.panorama",
                    GLFW.GLFW_KEY_F7,
                    "key.categories.misc"
            ));

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (SCREENSHOT_MENU_KEY.wasPressed()) client.setScreen(new ScreenshotScreen(null));
            while (PANORAMA_KEY.wasPressed()) client.takePanorama(client.runDirectory, 1024, 1024);
        });
    }
}