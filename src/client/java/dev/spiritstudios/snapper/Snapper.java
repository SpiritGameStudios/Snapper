package dev.spiritstudios.snapper;

import dev.spiritstudios.snapper.gui.ScreenshotScreen;
import dev.spiritstudios.snapper.gui.ScreenshotViewerScreen;
import dev.spiritstudios.snapper.util.*;
import dev.spiritstudios.specter.api.ModMenuHelper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

public class Snapper implements ClientModInitializer {
    public static final String MODID = "snapper";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
    public static final boolean IS_IRIS_INSTALLED = FabricLoader.getInstance().isModLoaded("iris");

    private static final KeyBinding SCREENSHOT_MENU_KEY = KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                    "key.snapper.screenshot_menu",
                    GLFW.GLFW_KEY_V,
                    "key.categories.misc"
            ));

    public static final KeyBinding RECENT_SCREENSHOT_KEY = KeyBindingHelper.registerKeyBinding(
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
        ModMenuHelper.addConfig(Snapper.MODID, SnapperConfig.INSTANCE.getId());

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (SCREENSHOT_MENU_KEY.wasPressed()) client.setScreen(new ScreenshotScreen(null));
            while (PANORAMA_KEY.wasPressed()) {
                if (client.player == null) continue;
                if (IS_IRIS_INSTALLED) {
                    client.player.sendMessage(Text.translatable("text.snapper.panorama_failure_iris"), true);
                    continue;
                }
                client.takePanorama(client.runDirectory, 1024, 1024);
                client.player.sendMessage(Text.translatable("text.snapper.panorama_success", SCREENSHOT_MENU_KEY.getBoundKeyLocalizedText()), true);
            }
            while (RECENT_SCREENSHOT_KEY.wasPressed()) {
                List<File> screenshots = ScreenshotActions.getScreenshots(client);
                if (screenshots.size() == 0) {
                    if (client.player != null) client.player.sendMessage(Text.translatable("text.snapper.screenshot_failure_open"), true);
                    continue;
                }
                File latestScreenshot = screenshots.getFirst();

                client.setScreen(new ScreenshotViewerScreen(
                        ScreenshotImage.of(latestScreenshot, client.getTextureManager()),
                        latestScreenshot,
                        null
                ));
            }
        });
    }

    public static PlatformHelper getPlatformHelper() {
        return switch (Util.getOperatingSystem()) {
            case WINDOWS -> new WindowsActions();
            case OSX -> new MacActions();
            default -> new WindowsActions();
        };
    }
}