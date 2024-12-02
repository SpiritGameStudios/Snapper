package dev.spiritstudios.snapper;

import dev.spiritstudios.snapper.gui.screen.ScreenshotScreen;
import dev.spiritstudios.snapper.gui.screen.ScreenshotViewerScreen;
import dev.spiritstudios.snapper.util.*;
import dev.spiritstudios.specter.api.config.ModMenuHelper;
import dev.spiritstudios.specter.api.core.util.ClientKeybindEvents;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

public class Snapper implements ClientModInitializer {
    public static final String MODID = "snapper";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public static final boolean IS_IRIS_INSTALLED = FabricLoader.getInstance().isModLoaded("iris");
    public static final KeyBinding RECENT_SCREENSHOT_KEY = KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                    "key.snapper.recent",
                    GLFW.GLFW_KEY_O,
                    "key.categories.misc"
            ));
    private static final KeyBinding SCREENSHOT_MENU_KEY = KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                    "key.snapper.screenshot_menu",
                    GLFW.GLFW_KEY_V,
                    "key.categories.misc"
            ));
    private static final KeyBinding PANORAMA_KEY = KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                    "key.snapper.panorama",
                    GLFW.GLFW_KEY_F8,
                    "key.categories.misc"
            ));

    public static PlatformHelper getPlatformHelper() {
        return switch (Util.getOperatingSystem()) {
            case WINDOWS -> new WindowsActions();
            case OSX -> new MacActions();
            default -> new WindowsActions();
        };
    }

    @Override
    public void onInitializeClient() {
        ModMenuHelper.addConfig(Snapper.MODID, SnapperConfig.HOLDER.id());

        ClientKeybindEvents.pressed(SCREENSHOT_MENU_KEY).register(client -> client.setScreen(new ScreenshotScreen(null)));

        ClientKeybindEvents.pressed(PANORAMA_KEY).register(client -> {
            if (client.player == null) return;

            if (IS_IRIS_INSTALLED) {
                client.player.sendMessage(Text.translatable("text.snapper.panorama_failure_iris"), true);
                return;
            }

            client.takePanorama(client.runDirectory, 1024, 1024);
            client.player.sendMessage(Text.translatable(
                    "text.snapper.panorama_success",
                    SCREENSHOT_MENU_KEY.getBoundKeyLocalizedText()
            ), true);
        });

        ClientKeybindEvents.pressed(RECENT_SCREENSHOT_KEY).register(client -> {
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
        });
    }

    public static Identifier id(String path) {
        return Identifier.of(MODID, path);
    }
}