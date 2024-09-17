package dev.spiritstudios.snapper.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import dev.spiritstudios.snapper.mixin.accessor.CameraAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.util.function.Consumer;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Final
    @Shadow
    public static boolean IS_SYSTEM_MAC;

    @Final
    @Shadow
    public GameOptions options;

    @Final
    @Shadow
    public GameRenderer gameRenderer;

    @WrapOperation(
            method = "takePanorama",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/util/ScreenshotRecorder;saveScreenshot(Ljava/io/File;Ljava/lang/String;Lnet/minecraft/client/gl/Framebuffer;Ljava/util/function/Consumer;)V")
    )
    private void saveScreenshot(File gameDirectory, @Nullable String fileName, Framebuffer framebuffer, Consumer<Text> messageReceiver, Operation<Void> original) {
        fileName = "panorama/" + fileName;
        original.call(gameDirectory, fileName, framebuffer, messageReceiver);
    }

    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    private void init(RunArgs args, CallbackInfo ci) {
        if (!IS_SYSTEM_MAC) System.setProperty("java.awt.headless", "false");
    }

    @WrapOperation(
            method = "takePanorama",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;setYaw(F)V")
    )
    private void captureSetYaw(ClientPlayerEntity player, float value, Operation<Void> op, @Share("yaw")LocalFloatRef yaw) {
        if (!this.options.getPerspective().isFirstPerson()) yaw.set(value);
        else op.call(player, value);
    }

    @WrapOperation(
            method = "takePanorama",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;setPitch(F)V")
    )
    private void applyThirdPersonCameraRotation(ClientPlayerEntity player, float value, Operation<Void> op, @Share("yaw")LocalFloatRef yaw) {
        if (!this.options.getPerspective().isFirstPerson())
            ((CameraAccessor) this.gameRenderer.getCamera()).invokeSetRotation(yaw.get(), value);
        else op.call(player, value);
    }

}
