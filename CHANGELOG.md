## Snapper 1.2.0

### Features
* You can now store more than one panorama at a time.
* Panoramas are now saved as a single file instead of multiple.

### Changes
* The overlay when using the file-picker dialogue now blurs the screen again.
    * This was originally removed due to a vanilla change making it impossible to blur the screen more than once per-frame. We have worked around this by cancelling the earlier blur if it existed.

### Bug Fixes
* Fix a very rare race condition when taking a panorama if the GPU took more than 10ms to render a single face of a panorama.
* Fix the game crashing or spamming the logs if a screenshot failed to load for any reason.