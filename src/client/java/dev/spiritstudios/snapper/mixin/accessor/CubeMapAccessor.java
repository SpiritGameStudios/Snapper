package dev.spiritstudios.snapper.mixin.accessor;

import com.mojang.blaze3d.buffers.GpuBuffer;
import net.minecraft.client.renderer.CubeMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(CubeMap.class)
public interface CubeMapAccessor {
    @Invoker
    static GpuBuffer callInitializeVertices() {
        throw new UnsupportedOperationException("Implemented via mixin.");
    }
}
