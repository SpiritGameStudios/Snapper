package dev.spiritstudios.snapper.mixin.accessor;

import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PictureInPictureRenderer.class)
public interface PictureInPictureRendererAccessor {
    @Accessor
    @Nullable GpuTexture getTexture();

    @Accessor
    @Nullable GpuTextureView getTextureView();

    @Accessor
    @Nullable GpuTextureView getDepthTextureView();

    @Invoker
    void callPrepareTexturesAndProjection(final boolean needsAResize, final int width, final int height);
}
