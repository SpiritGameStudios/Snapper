package dev.spiritstudios.snapper.mixin;

import dev.spiritstudios.snapper.render.SnapperGuiGraphicsExtractor;
import dev.spiritstudios.snapper.render.panorama.GuiPanoramaRenderState;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(GuiGraphicsExtractor.class)
public class GuiGraphicsExtractorMixin implements SnapperGuiGraphicsExtractor {
    @Shadow
    @Final
    public GuiRenderState guiRenderState;

    @Shadow
    @Final
    public GuiGraphicsExtractor.ScissorStack scissorStack;

    @Override
    public void snapper$panorama(float spin, Identifier textureLocation, int x0, int y0, int x1, int y1) {
        this.guiRenderState.addPicturesInPictureState(
                new GuiPanoramaRenderState(
                        spin,
                        textureLocation,
                        x0, y0,
                        x1, y1,
                        1F,
                        this.scissorStack.peek()
                )
        );
    }
}
