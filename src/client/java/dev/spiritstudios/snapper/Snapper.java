package dev.spiritstudios.snapper;

import dev.spiritstudios.snapper.util.MacActions;
import dev.spiritstudios.snapper.util.PlatformHelper;
import dev.spiritstudios.snapper.util.WindowsActions;
import dev.spiritstudios.snapper.util.uploading.ScreenshotUploading;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.polyfrost.oneconfig.api.event.v1.EventManager;
import org.polyfrost.oneconfig.api.event.v1.events.ShutdownEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public final class Snapper {
    public static final String MODID = SnapperConstants.ID;
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public static Optional<Boolean> IS_IRIS_INSTALLED = Optional.empty();

    public void onInitializeClient() {
        SnapperKeybindings.init();

        EventManager.register(ShutdownEvent.class, event -> ScreenshotUploading.close());
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