package dev.spiritstudios.snapper.util.clipboard;

import dev.spiritstudios.snapper.util.clipboard.linux.PathTraverser;
import dev.spiritstudios.snapper.util.clipboard.linux.WaylandClipboard;
import dev.spiritstudios.snapper.util.clipboard.linux.X11Clipboard;
import net.minecraft.util.Util;

import java.nio.file.Path;
import java.util.Locale;

public interface Clipboard {
    Clipboard INSTANCE = switch (Util.getPlatform()) {
        case LINUX, SOLARIS, UNKNOWN -> {
            String sessionType = System.getenv("XDG_SESSION_TYPE");
            String wlDisplay = System.getenv("WAYLAND_DISPLAY");
            if (((sessionType != null && sessionType.strip().equalsIgnoreCase("wayland")) || (wlDisplay != null && !wlDisplay.isEmpty())) &&
                    PathTraverser.isOnPath("wl-copy")) {
                yield new WaylandClipboard();
            } else if (PathTraverser.isOnPath("xclip")) {
                yield new X11Clipboard();
            }

            yield new AWTClipboard();
        }
        case WINDOWS -> new AWTClipboard();
        case OSX -> new MacClipboard();
    };

    boolean copyScreenshot(Path screenshot);
}
