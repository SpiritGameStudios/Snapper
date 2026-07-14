package dev.spiritstudios.snapper.util.clipboard.linux;

import java.util.List;

public class WaylandClipboard extends ProcessClipboard {
    @Override
    protected List<String> command() {
        return List.of("wl-copy", "--type", "image/png");
    }
}
