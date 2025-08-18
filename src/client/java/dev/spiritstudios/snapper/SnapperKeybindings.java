package dev.spiritstudios.snapper;

import dev.deftu.omnicore.client.OmniChat;
import dev.deftu.omnicore.client.OmniScreen;
import dev.deftu.omnicore.client.keybindings.ManagedKeyBinding;
import dev.deftu.omnicore.client.keybindings.OmniKeyBinding;
import dev.deftu.omnicore.common.OmniLoader;
import dev.deftu.textile.minecraft.MCTextHolder;
import dev.spiritstudios.snapper.gui.screen.ScreenshotScreen;
import dev.spiritstudios.snapper.gui.screen.ScreenshotViewerScreen;
import dev.spiritstudios.snapper.util.DynamicTexture;
import dev.spiritstudios.snapper.util.ScreenshotActions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.polyfrost.oneconfig.api.event.v1.EventManager;
import org.polyfrost.oneconfig.api.event.v1.events.KeyInputEvent;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public final class SnapperKeybindings {
    public static final ManagedKeyBinding PANORAMA_KEY = OmniKeyBinding.create(
            "key.snapper.panorama",
            "key.categories.snapper",
            GLFW.GLFW_KEY_F8
    );

    public static final ManagedKeyBinding RECENT_SCREENSHOT_KEY = OmniKeyBinding.create(
            "key.snapper.recent",
            "key.categories.snapper",
            GLFW.GLFW_KEY_O
    );

    public static final ManagedKeyBinding SCREENSHOT_MENU_KEY = OmniKeyBinding.create(
            "key.snapper.screenshot_menu",
            "key.categories.snapper",
            GLFW.GLFW_KEY_V
    );

    public static void init() {
        SCREENSHOT_MENU_KEY.register();
        PANORAMA_KEY.register();
        RECENT_SCREENSHOT_KEY.register();
        EventManager.register(KeyInputEvent.class, (event) -> { //todo replace with omnicore keybind methods once that gets pushed to oneconfig
            if (SCREENSHOT_MENU_KEY.consume()) {
                OmniScreen.setCurrentScreen(new ScreenshotScreen(OmniScreen.getCurrentScreen()));
            }
            if (PANORAMA_KEY.consume()) {
                takePanorama(MinecraftClient.getInstance());
            }
            if (RECENT_SCREENSHOT_KEY.consume()) {
                openRecentScreenshot(MinecraftClient.getInstance());
            }
        });
    }

    private static void takePanorama(MinecraftClient client) {
        if (client.player == null) return;

        if (Snapper.IS_IRIS_INSTALLED.orElseGet(() -> { Snapper.IS_IRIS_INSTALLED = Optional.of(OmniLoader.isModLoaded("iris")); return Snapper.IS_IRIS_INSTALLED.get(); })) { // TODO migrate all usages of "displayClientMessage" to use action bar/overlay
            OmniChat.displayClientMessage(MCTextHolder.convertFromVanilla(Text.translatable("text.snapper.panorama_failure_iris")));
            return;
        }

        client.takePanorama(client.runDirectory, 1024, 1024);
        OmniChat.displayClientMessage(MCTextHolder.convertFromVanilla(Text.translatable(
                "text.snapper.panorama_success",
                SCREENSHOT_MENU_KEY.getVanillaKeyBinding().getBoundKeyLocalizedText()
        )));
    }

    private static void openRecentScreenshot(MinecraftClient client) {
        List<Path> screenshots = ScreenshotActions.getScreenshots();
        if (screenshots.isEmpty()) {
            if (client.player != null)
                OmniChat.displayClientMessage(MCTextHolder.convertFromVanilla(Text.translatable("text.snapper.screenshot_not_exists")));
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
                        () -> {
                            if (client.player != null)
                                client.player.sendMessage(Text.translatable("text.snapper.screenshot_open_failure"), true);
                        }
                );

    }
}
