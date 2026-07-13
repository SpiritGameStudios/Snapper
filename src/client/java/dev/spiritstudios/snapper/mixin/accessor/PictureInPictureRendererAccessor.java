package dev.spiritstudios.snapper.mixin.accessor;

import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PictureInPictureRenderer.class)
public interface PictureInPictureRendererAccessor {
    @Accessor
    @Nullable GpuTexture getTexture();

    @Accessor
    @Nullable GpuTextureView getTextureView();

    @Accessor
    @Nullable GpuTextureView getDepthTextureView();
}
