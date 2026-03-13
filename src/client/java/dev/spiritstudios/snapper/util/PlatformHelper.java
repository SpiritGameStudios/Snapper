package dev.spiritstudios.snapper.util;

import dev.spiritstudios.snapper.util.actions.GeneralPlatformActions;
import dev.spiritstudios.snapper.util.actions.MacPlatformActions;
import net.minecraft.Util;

import java.nio.file.Path;

public interface PlatformHelper {
    PlatformHelper INSTANCE = Util.getPlatform() == Util.OS.OSX ?
            new MacPlatformActions() :
            new GeneralPlatformActions();

    void copyScreenshot(Path screenshot);
}
