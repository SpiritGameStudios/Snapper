package dev.spiritstudios.snapper.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import dev.spiritstudios.snapper.Snapper;
import dev.spiritstudios.snapper.SnapperConfig;
import dev.spiritstudios.snapper.util.PlatformHelper;
import net.minecraft.client.Screenshot;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Consumer;

@Mixin(Screenshot.class)
public abstract class ScreenshotRecorderMixin {
    /**
     * @author hama
     * @reason check if panorama file exists before writing to it
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Inject(
            method = "method_22691",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/NativeImage;writeToFile(Ljava/io/File;)V")
    )
    private static void lookBeforeYouLeap(NativeImage nativeImage, File screenshotFile, Consumer<Component> messageReceiver, CallbackInfo ci) throws IOException {
        screenshotFile.getParentFile().mkdirs();
        screenshotFile.createNewFile();
    }

    @Inject(
            method = "method_22691",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/Component;literal(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;", shift = At.Shift.AFTER)
    )
    private static void saveWrittenFileToClipboard(NativeImage nativeImage, File screenshotFile, Consumer<Component> messageReceiver, CallbackInfo ci) {
        if (!screenshotFile.getAbsolutePath().contains("/panorama/") && SnapperConfig.HOLDER.get().copyTakenScreenshot()) {
            PlatformHelper.INSTANCE.copyScreenshot(screenshotFile.toPath());
        }
    }

    /**
     * @author WorldWidePixel
     * @reason Okay, I know this is weird but it's so we can use our own wrapper text for the push.
     */
    @ModifyArg(method = "method_22691",
    at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/network/chat/MutableComponent;", ordinal = 0))
    private static String changeSuccessTranslation(String existing) {
        return "toast.snapper.screenshot.created.success";
    }

    @WrapMethod(method = "grab(Ljava/io/File;Ljava/lang/String;Lcom/mojang/blaze3d/pipeline/RenderTarget;ILjava/util/function/Consumer;)V")
    private static void getConfiguredGameDirectory(File gameDirectory, String fileName, RenderTarget renderTarget, int downscaleFactor, Consumer<Component> messageReceiver, Operation<Void> original) {
        original.call(
                SnapperConfig.HOLDER.get().customScreenshotPath().enabled() && Files.exists(SnapperConfig.HOLDER.get().customScreenshotPath().path()) ?
                        SnapperConfig.HOLDER.get().customScreenshotPath().path().toFile() :
                        gameDirectory,
                fileName,
                renderTarget,
                downscaleFactor,
                messageReceiver
        );
    }
}
