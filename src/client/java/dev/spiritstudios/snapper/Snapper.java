package dev.spiritstudios.snapper;

import dev.spiritstudios.snapper.util.PlatformHelper;
import dev.spiritstudios.snapper.util.actions.GeneralPlatformActions;
import dev.spiritstudios.snapper.util.actions.MacPlatformActions;
import dev.spiritstudios.snapper.util.config.DirectoryConfigUtil;
import dev.spiritstudios.snapper.util.uploading.ScreenshotUploading;
import dev.spiritstudios.specter.api.config.client.ConfigScreenWidgets;
import dev.spiritstudios.specter.api.config.client.ModMenuHelper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public final class Snapper implements ClientModInitializer {
    public static final String MODID = "snapper";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    @Override
    public void onInitializeClient() {
        ConfigScreenWidgets.add(Path.class, DirectoryConfigUtil.PATH_WIDGET_FACTORY);
        SnapperKeybindings.init();

        ModMenuHelper.addConfig(Snapper.MODID, SnapperConfig.HOLDER.id());

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> ScreenshotUploading.close());
    }

    public static Identifier id(String path) {
        return Identifier.of(MODID, path);
    }

    public static PlatformHelper getPlatformHelper() {
        return MinecraftClient.IS_SYSTEM_MAC ? new MacPlatformActions() : new GeneralPlatformActions();
    }
}