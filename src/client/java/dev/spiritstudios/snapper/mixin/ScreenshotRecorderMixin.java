package dev.spiritstudios.snapper.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.spiritstudios.snapper.Snapper;
import dev.spiritstudios.snapper.SnapperConfig;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Consumer;

@Mixin(ScreenshotRecorder.class)
public abstract class ScreenshotRecorderMixin {
    /**
     * @author hama
     * @reason check if panorama file exists before writing to it
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Inject(
            method = "method_22691",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/texture/NativeImage;writeTo(Ljava/io/File;)V")
    )
    private static void lookBeforeYouLeap(NativeImage nativeImage, File screenshotFile, Consumer<Text> messageReceiver, CallbackInfo ci) throws IOException {
        screenshotFile.getParentFile().mkdirs();
        screenshotFile.createNewFile();
    }

    @Inject(
            method = "method_22691",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/text/Text;literal(Ljava/lang/String;)Lnet/minecraft/text/MutableText;", shift = At.Shift.AFTER)
    )
    private static void saveWrittenFileToClipboard(NativeImage nativeImage, File screenshotFile, Consumer<Text> messageReceiver, CallbackInfo ci) {
        if (!screenshotFile.getAbsolutePath().contains("/panorama/") && SnapperConfig.INSTANCE.copyTakenScreenshot.get()) {
            Snapper.getPlatformHelper().copyScreenshot(screenshotFile.toPath());
        }
    }

    @WrapMethod(method = "saveScreenshot(Ljava/io/File;Ljava/lang/String;Lnet/minecraft/client/gl/Framebuffer;Ljava/util/function/Consumer;)V")
    private static void getConfiguredGameDirectory(File gameDirectory, @Nullable String fileName, Framebuffer framebuffer, Consumer<Text> messageReceiver, Operation<Void> original) {
        original.call(
                SnapperConfig.INSTANCE.useCustomScreenshotFolder.get() && Files.exists(SnapperConfig.INSTANCE.customScreenshotFolder.get()) ?
                        SnapperConfig.INSTANCE.customScreenshotFolder.get().toFile() :
                        gameDirectory,
                fileName,
                framebuffer,
                messageReceiver
        );
    }
}
