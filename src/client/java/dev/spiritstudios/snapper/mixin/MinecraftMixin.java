package dev.spiritstudios.snapper.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import com.mojang.blaze3d.pipeline.RenderTarget;
import dev.spiritstudios.snapper.SnapperConfig;
import dev.spiritstudios.snapper.mixin.accessor.CameraAccessor;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.util.function.Consumer;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Final
    @Shadow
    public Options options;

    @Final
    @Shadow
    public GameRenderer gameRenderer;

    @WrapOperation(
            method = "grabPanoramixScreenshot",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Screenshot;grab(Ljava/io/File;Ljava/lang/String;Lcom/mojang/blaze3d/pipeline/RenderTarget;ILjava/util/function/Consumer;)V")
    )
    private void saveScreenshot(File gameDirectory, String fileName, RenderTarget renderTarget, int downscaleFactor, Consumer<Component> messageReceiver, Operation<Void> original) {
        fileName = "panorama/" + fileName;
        original.call(gameDirectory, fileName, renderTarget, downscaleFactor, messageReceiver);
    }

    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    private void init(GameConfig args, CallbackInfo ci) {
        if (!Util.getPlatform().equals(Util.OS.OSX)) System.setProperty("java.awt.headless", "false");
    }

    @WrapOperation(
            method = "grabPanoramixScreenshot",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;setYRot(F)V")
    )
    private void captureSetYaw(LocalPlayer player, float value, Operation<Void> op, @Share("yaw")LocalFloatRef yaw) {
        if (!this.options.getCameraType().isFirstPerson()) yaw.set(value);
        else op.call(player, value);
    }

    @WrapOperation(
            method = "grabPanoramixScreenshot",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;setYRot(F)V")
    )
    private void applyThirdPersonCameraRotation(LocalPlayer player, float value, Operation<Void> op, @Share("yaw")LocalFloatRef yaw) {
        if (!this.options.getCameraType().isFirstPerson())
            ((CameraAccessor) this.gameRenderer.getMainCamera()).invokeSetRotation(yaw.get(), value);
        else op.call(player, value);
    }

    @ModifyConstant(
            method = "grabPanoramixScreenshot",
            constant = @Constant(intValue = 4096)
    )
    private int configurablePanoramaSize(int original) {
        return SnapperConfig.HOLDER.get().panoramaDimensions().size() * 4;
    }

}
