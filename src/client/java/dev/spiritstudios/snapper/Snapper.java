package dev.spiritstudios.snapper;

import dev.spiritstudios.snapper.render.panorama.GuiPanoramaRenderer;
import dev.spiritstudios.snapper.util.uploading.ScreenshotUploading;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.PictureInPictureRendererRegistry;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Snapper implements ClientModInitializer {
    public static final String MOD_ID = "snapper";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        SnapperConfig.init();
        SnapperKeyMappings.init();
        ClientLifecycleEvents.CLIENT_STOPPING.register(_ -> ScreenshotUploading.close());
        PictureInPictureRendererRegistry.register(_ -> new GuiPanoramaRenderer());
    }

    public static SpriteIconButton createSnapperButton(final int width, final Button.OnPress onPress) {
        return SpriteIconButton.builder(
                        Component.translatable("button.snapper.gallery"),
                        onPress,
                        true
                )
                .width(width)
                .sprite(Snapper.id("screenshots/screenshot"), 15, 15)
                .tooltip(Component.translatable("button.snapper.gallery"))
                .narration(_ -> Component.translatable("button.snapper.gallery"))
                .build();
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}