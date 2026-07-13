package dev.spiritstudios.snapper.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import dev.spiritstudios.snapper.render.panorama.GuiPanoramaRenderer;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.CubeMap;
import net.minecraft.client.renderer.Projection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PictureInPictureRenderer.class)
public class PictureInPictureRendererMixin {
    @WrapOperation(method = "prepareTexturesAndProjection", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/Projection;setupOrtho(FFFFZ)V"))
    private void setupProjection(Projection instance, float zNear, float zFar, float width, float height, boolean invertY, Operation<Void> original) {
        if ((Object)this instanceof GuiPanoramaRenderer) {
            instance.setupPerspective(CubeMap.PROJECTION_Z_NEAR, CubeMap.PROJECTION_Z_FAR, CubeMap.PROJECTION_FOV, width, height);
        } else {
            original.call(instance, zNear, zFar, width, height, invertY);
        }
    }

    @WrapOperation(method = "prepareTexturesAndProjection", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setProjectionMatrix(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lcom/mojang/blaze3d/ProjectionType;)V"))
    private void setProjection(GpuBufferSlice projectionMatrixBuffer, ProjectionType type, Operation<Void> original) {
        if ((Object)this instanceof GuiPanoramaRenderer) {
            original.call(projectionMatrixBuffer, ProjectionType.PERSPECTIVE);
        } else {
            original.call(projectionMatrixBuffer, type);
        }
    }
}
