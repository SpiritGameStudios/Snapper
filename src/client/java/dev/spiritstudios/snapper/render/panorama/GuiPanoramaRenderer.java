package dev.spiritstudios.snapper.render.panorama;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.spiritstudios.snapper.mixin.accessor.CubeMapAccessor;
import dev.spiritstudios.snapper.mixin.accessor.PictureInPictureRendererAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

import java.util.Optional;
import java.util.OptionalDouble;

public class GuiPanoramaRenderer extends PictureInPictureRenderer<GuiPanoramaRenderState> {
    private final GpuBuffer vertexBuffer;

    public GuiPanoramaRenderer() {
        super();

        this.vertexBuffer = CubeMapAccessor.callInitializeVertices();
    }

    @Override
    public void prepare(GuiPanoramaRenderState renderState, GuiRenderState guiRenderState, FeatureRenderDispatcher featureRenderDispatcher, int guiScale) {
        PictureInPictureRendererAccessor accessor = (PictureInPictureRendererAccessor) this;
        GpuTexture drawTexture = accessor.getTexture();

        int width = (renderState.x1() - renderState.x0()) * guiScale;
        int height = (renderState.y1() - renderState.y0()) * guiScale;
        boolean needsAResize = drawTexture == null || drawTexture.getWidth(0) != width || drawTexture.getHeight(0) != height;
        if (!needsAResize && this.textureIsReadyToBlit(renderState)) {
            this.blitTexture(renderState, guiRenderState);
        } else {
            this.prepareTexturesAndProjection(needsAResize, width, height);

            Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
            modelViewStack.pushMatrix();
            modelViewStack.rotateX(Mth.PI + (10F * Mth.DEG_TO_RAD));
            modelViewStack.rotateY(renderState.spin() * Mth.DEG_TO_RAD);

            GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms().writeTransform(new Matrix4f(modelViewStack));
            modelViewStack.popMatrix();

            RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS);
            GpuBuffer indexBuffer = indices.getBuffer(36);

            try (RenderPass renderPass = RenderSystem.getDevice()
                    .createCommandEncoder()
                    .createRenderPass(
                            () -> "Cubemap",
                            accessor.getTextureView(),
                            Optional.empty(),
                            accessor.getDepthTextureView(),
                            OptionalDouble.empty()
                    )
            ) {
                renderPass.setPipeline(RenderPipelines.PANORAMA);
                RenderSystem.bindDefaultUniforms(renderPass);
                renderPass.setVertexBuffer(0, this.vertexBuffer.slice());
                renderPass.setIndexBuffer(indexBuffer, indices.type());
                renderPass.setUniform("DynamicTransforms", dynamicTransforms);
                AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(renderState.textureLocation());
                renderPass.bindTexture("Sampler0", texture.getTextureView(), texture.getSampler());
                renderPass.drawIndexed(36, 1, 0, 0, 0);
            }

            this.blitTexture(renderState, guiRenderState);
        }
    }

    @Override
    public Class<GuiPanoramaRenderState> getRenderStateClass() {
        return GuiPanoramaRenderState.class;
    }

    @Override
    protected void renderToTexture(GuiPanoramaRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        // Rendering is handled in prepare
    }

    @Override
    protected String getTextureLabel() {
        return "panorama";
    }

    @Override
    public void close() {
        super.close();
        this.vertexBuffer.close();
    }
}
