package dev.spiritstudios.snapper.render.panorama;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.spiritstudios.snapper.Snapper;
import dev.spiritstudios.snapper.SnapperConfig;
import dev.spiritstudios.snapper.SnapperKeyMappings;
import dev.spiritstudios.snapper.gui.toast.SnapperToast;
import dev.spiritstudios.snapper.gui.toast.SnapperToasts;
import dev.spiritstudios.snapper.util.ScreenshotActions;
import dev.spiritstudios.snapper.util.SnapperUtil;
import net.minecraft.client.*;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;

import java.nio.file.Path;

public final class PanoramaGrabber {
    public static void grabSnapperPanorama(Minecraft minecraft) {
        int faceSize = SnapperConfig.get().panorama().dimensions().size();
        int superSampling = SnapperConfig.get().panorama().superSampling();
        int outputWidth = faceSize * 3;
        int outputHeight = faceSize * 2;

        Window window = minecraft.getWindow();

        int oldWidth = window.getWidth();
        int oldHeight = window.getHeight();
        CameraType oldCameraType = minecraft.options.getCameraType();
        RenderTarget target = minecraft.gameRenderer.mainRenderTarget();
        float xRot = minecraft.player.getXRot();
        float yRot = minecraft.player.getYRot();
        float xRotO = minecraft.player.xRotO;
        float yRotO = minecraft.player.yRotO;
        minecraft.gameRenderer.setRenderBlockOutline(false);
        Camera camera = minecraft.gameRenderer.mainCamera();

        try {
            minecraft.options.setCameraType(CameraType.FIRST_PERSON);
            camera.enablePanoramicMode();
            window.setWidth(faceSize * superSampling);
            window.setHeight(faceSize * superSampling);
            target.resize(faceSize * superSampling, faceSize * superSampling);

            NativeImage output = new NativeImage(outputWidth, outputHeight, false);

            renderFace(minecraft, output, target, faceSize, yRot, 90.0F, 0, 0); // BOTTOM
            renderFace(minecraft, output, target, faceSize, yRot, -90.0F, 1, 0); // TOP
            renderFace(minecraft, output, target, faceSize, (yRot + 180.0F) % 360.0F, 0F, 2, 0); // BACK

            renderFace(minecraft, output, target, faceSize, (yRot - 90.0F) % 360.0F, 0F, 0, 1); // LEFT
            renderFace(minecraft, output, target, faceSize, yRot, 0F, 1, 1); // FRONT
            renderFace(minecraft, output, target, faceSize, (yRot + 90.0F) % 360.0F, 0F, 2, 1); // RIGHT

            RenderSystem.queueFencedTask(() ->
                    Util.ioPool().execute(() -> {
                        try (output) {
                            Path path = ScreenshotActions
                                    .getPanoramaDirectory()
                                    .resolve(Util.getFilenameFormattedDateTime() + ".png");

                            output.writeToFile(path);

                            SnapperToasts.panoramaCreateSuccess(Component.literal(path.getFileName().toString()));
                        } catch (Exception e) {
                            Snapper.LOGGER.warn("Couldn't save screenshot", e);
                        }
                    })
            );
        } catch (Throwable e) {
            Snapper.LOGGER.error("Failed to take panorama", e);
        } finally {
            minecraft.player.setXRot(xRot);
            minecraft.player.setYRot(yRot);
            minecraft.player.xRotO = xRotO;
            minecraft.player.yRotO = yRotO;
            minecraft.gameRenderer.setRenderBlockOutline(true);
            window.setWidth(oldWidth);
            window.setHeight(oldHeight);
            target.resize(oldWidth, oldHeight);
            camera.disablePanoramicMode();
            minecraft.options.setCameraType(oldCameraType);
        }
    }

    private static void renderFace(
            Minecraft minecraft,
            NativeImage output,
            RenderTarget target,
            int faceSize,
            float yRot, float xRot, int offX, int offY
    ) {
        minecraft.player.setYRot(yRot);
        minecraft.player.setXRot(xRot);
        minecraft.player.yRotO = minecraft.player.getYRot();
        minecraft.player.xRotO = minecraft.player.getXRot();

        minecraft.gameRenderer.update(DeltaTracker.ONE);
        minecraft.gameRenderer.extract(DeltaTracker.ONE, true);
        minecraft.gameRenderer.renderLevel(DeltaTracker.ONE);

        Screenshot.takeScreenshot(target, SnapperConfig.get().panorama().superSampling(), image -> {
            image.copyRect(
                    output,
                    0, 0,
                    offX * faceSize, offY * faceSize,
                    image.getWidth(), image.getHeight(),
                    false, false
            );
        });
    }
}
