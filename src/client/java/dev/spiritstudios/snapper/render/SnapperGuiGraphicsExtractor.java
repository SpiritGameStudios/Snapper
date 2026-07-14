package dev.spiritstudios.snapper.render;

import net.minecraft.resources.Identifier;

public interface SnapperGuiGraphicsExtractor {
    default void snapper$panorama(
            float spin,
            Identifier textureLocation,
            int x0, int y0,
            int x1, int y1
    ) {
        throw new UnsupportedOperationException("Implemented via mixin.");
    }

    default void snapper$forceBlurBeforeThisStratum() {
        throw new UnsupportedOperationException("Implemented via mixin.");
    }
}
