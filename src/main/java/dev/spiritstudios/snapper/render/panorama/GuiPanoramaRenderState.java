package dev.spiritstudios.snapper.render.panorama;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public record GuiPanoramaRenderState(
        float spin,
        Identifier textureLocation,
        int x0, int y0,
        int x1, int y1,
        float scale,
        @Nullable ScreenRectangle scissorArea,
        @Nullable ScreenRectangle bounds
) implements PictureInPictureRenderState {
    public GuiPanoramaRenderState(
            float spin,
            Identifier textureLocation,
            int x0, int y0,
            int x1, int y1,
            float scale,
            @Nullable ScreenRectangle scissorArea
    ) {
        this(spin, textureLocation, x0, y0, x1, y1, scale, scissorArea, PictureInPictureRenderState.getBounds(x0, y0, x1, y1, scissorArea));
    }
}
