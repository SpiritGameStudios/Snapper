package dev.spiritstudios.snapper;

import dev.spiritstudios.snapper.util.PlatformHelper;
import dev.spiritstudios.snapper.util.actions.GeneralPlatformActions;
import dev.spiritstudios.snapper.util.actions.MacPlatformActions;
import dev.spiritstudios.snapper.util.uploading.ScreenshotUploading;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

public final class Snapper implements ClientModInitializer {
    public static final String MODID = "snapper";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    @Override
    public void onInitializeClient() {
        SnapperConfig.init();
        SnapperKeybindings.init();
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> ScreenshotUploading.close());
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    public static PlatformHelper getPlatformHelper() {
        return Util.getPlatform() == Util.OS.OSX ? new MacPlatformActions() : new GeneralPlatformActions();
    }
}