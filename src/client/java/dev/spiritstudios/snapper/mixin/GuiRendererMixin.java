package dev.spiritstudios.snapper.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import dev.spiritstudios.snapper.Snapper;
import dev.spiritstudios.snapper.gui.screen.PanoramaViewerScreen;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.CubeMap;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.state.gui.PanoramaRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiRenderer.class)
public class GuiRendererMixin {
    @Shadow
    @Final
    private GuiRenderState renderState;

    @Shadow
    @Final
    private CubeMap cubeMap;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/lang/String;)V", ordinal = 0))
    private void renderSnapperPanorama(GpuBufferSlice fogBuffer, CallbackInfo ci) {
        PanoramaRenderState pano = renderState.getData(PanoramaViewerScreen.SNAPPER_PANORAMA);

        if (pano != null) {
            ScopedValue.where(Snapper.CUBEMAP_TEXTURE, PanoramaViewerScreen.TEXTURE_ID).run(() -> {
                this.cubeMap.render(10.0F, pano.spin());
            });
        }
    }
}
