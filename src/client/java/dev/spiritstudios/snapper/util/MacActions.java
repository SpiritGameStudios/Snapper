package dev.spiritstudios.snapper.util;

import ca.weblite.objc.Client;
import ca.weblite.objc.Proxy;
import dev.spiritstudios.snapper.Snapper;

import java.io.File;
import java.nio.file.Path;

public class MacActions implements PlatformHelper {

    /* Screenshot copy logic (ScreenshotActions, ScreenshotActionsMac) heavily inspired by
    ScreenshotViewer by LGatodu47. (https://github.com/LGatodu47/ScreenshotViewer).
    Including their license here because it seems like the right thing to do.
    <3 - WorldWidePixel */

    /*
    The MIT License (MIT)

    Copyright (c) 2024 LGatodu47

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in
    all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
    THE SOFTWARE.
    */

    // Code in ScreenshotActionsMac taken from ScreenshotToClipboard: https://github.com/comp500/ScreenshotToClipboard
    /*
    MIT License

    Copyright (c) 2018 comp500

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
    */

    @Override
    public void copyScreenshot(Path screenshot) {
        Client client = Client.getInstance();
        Proxy url = client.sendProxy("NSURL", "fileURLWithPath:", screenshot.toAbsolutePath().toString());

        Proxy image = client.sendProxy("NSImage", "alloc");
        image.send("initWithContentsOfURL:", url);

        Proxy array = client.sendProxy("NSArray", "array");
        array = array.sendProxy("arrayByAddingObject:", image);

        Proxy pasteboard = client.sendProxy("NSPasteboard", "generalPasteboard");
        pasteboard.send("clearContents");
        boolean wasSuccessful = pasteboard.sendBoolean("writeObjects:", array);
        if (!wasSuccessful) {
            Snapper.LOGGER.error("Failed to write image to pasteboard.");
        }
    }
}
