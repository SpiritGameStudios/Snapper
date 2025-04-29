package dev.spiritstudios.snapper;

import dev.spiritstudios.snapper.util.MacActions;
import dev.spiritstudios.snapper.util.PlatformHelper;
import dev.spiritstudios.snapper.util.WindowsActions;
import dev.spiritstudios.snapper.util.uploading.ScreenshotUploading;
import dev.spiritstudios.specter.api.config.ModMenuHelper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Snapper implements ClientModInitializer {
    public static final String MODID = "snapper";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public static final boolean IS_IRIS_INSTALLED = FabricLoader.getInstance().isModLoaded("iris");

    @Override
    public void onInitializeClient() {
        SnapperKeybindings.init();

        ModMenuHelper.addConfig(Snapper.MODID, SnapperConfig.HOLDER.id());

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> ScreenshotUploading.close());
    }

    public static Identifier id(String path) {
        return Identifier.of(MODID, path);
    }

    public static PlatformHelper getPlatformHelper() {
        return switch (Util.getOperatingSystem()) {
            case WINDOWS -> new WindowsActions();
            case OSX -> new MacActions();
            default -> new WindowsActions();
        };
    }
}