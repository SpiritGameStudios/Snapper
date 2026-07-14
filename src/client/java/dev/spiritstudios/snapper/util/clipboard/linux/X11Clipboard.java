package dev.spiritstudios.snapper.util.clipboard.linux;

import java.util.List;

public class X11Clipboard extends ProcessClipboard {
    @Override
    protected List<String> command() {
        return List.of(
                "xclip",
                "-selection",
                "clipboard",
                "-t",
                "image/png",
                "-i"
        );
    }
}
