## Snapper 1.2.0

### Features
* You can now store more than one panorama at a time.
* Panoramas are now saved as a single file instead of multiple.
* There are now more toasts for certain errors.
* You can now enable a button to capture an in-game screenshot from the game menu.
* Panorama super-sampling is now configurable (Defaults to 4x).

### Changes
* **Moved the default unified screenshot directory to match other apps.**
    * Windows: `%APPDATA%/snapper` -> `%APPDATA%/snapper`
    * Linux: `$HOME/.snapper` -> `$HOME/.local/share/snapper`
    * macOS: Unchanged
* The overlay when using the file-picker dialogue now blurs the screen again.
    * This was originally removed due to a vanilla change making it impossible to blur the screen more than once per-frame. We have worked around this by cancelling the earlier blur if it existed.
* Use the command line tools `wl-copy` or `xclip` instead of AWT to copy images if they are available
    * This should make Auto-clipboard work without any setup on most Linux systems.

### Bug Fixes
* Fix a very rare race condition when taking a panorama if the GPU took more than 10ms to render a single face of a panorama.
* Fix the game crashing or spamming the logs if a screenshot failed to load for any reason.
* Fix inconsistent use of icons in toasts.
* Fix inconsistent text in toasts.
* Fix keyboard input for sliders on the config screen.

### Translation Changes
All translation changes have only made it to the en_us language file at this time.
- `button.snapper.screenshots` is now `button.snapper.gallery`, and the menu has been renamed to the "Screenshot Gallery".
- `menu.snapper.screenshot_menu` is now `menu.snapper.gallery`, and the menu has been renamed to "Screenshot Gallery".
- `key.snapper.screenshot_menu` is now `key.snapper.open_gallery`, and the title has been renamed to "Open Gallery".
- "Take Panoramic Screenshots" is now "Take Panorama" (`key.snapper.panorama`)
- `menu.snapper.panorama` ("View Panorama") has been removed and functionally replaced with `menu.snapper.tab.panoramas` ("Panoramas").
- `text.snapper.empty` is now `text.snapper.empty.screenshot`, to account for two contexts of which near-identical text may be seen.
- Added `text.snapper.empty.panorama`, to display specific information for empty panorama galleries.
- Added `button.snapper.helper.screenshot` for a button in the Game Menu which lets users take screenshots without using the keyboard.
- Added `subtitles.snapper.ui.shutter` for the sound that plays when Capture Screenshot is pressed from the Game Menu.
- Added `config.snapper.panoramaSuperSampling` for the super sampling slider in the config menu.
- Added `config.snapper.panoramaSuperSampling.tooltip` for the tooltip of the super sampling slider in the config menu.
- Added `config.snapper.panoramaSuperSampling.value` for the value of the super sampling slider in the config menu.