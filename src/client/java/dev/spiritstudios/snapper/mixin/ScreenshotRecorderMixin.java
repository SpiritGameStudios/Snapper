package dev.spiritstudios.snapper.mixin;

import dev.spiritstudios.snapper.SnapperConfig;
import dev.spiritstudios.snapper.mixinsupport.ImageTransferable;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

@Mixin(ScreenshotRecorder.class)
public abstract class ScreenshotRecorderMixin {
    /**
     * @author hama
     * @reason check if panorama file exists before writing to it
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Inject(
            method = "method_1661",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/texture/NativeImage;writeTo(Ljava/io/File;)V")
    )
    private static void lookBeforeYouLeap(NativeImage nativeImage, File screenshotFile, Consumer<Text> messageReceiver, CallbackInfo ci) throws IOException {
        screenshotFile.getParentFile().mkdirs();
        screenshotFile.createNewFile();
    }

    @Inject(
            method = "method_1661",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/text/Text;literal(Ljava/lang/String;)Lnet/minecraft/text/MutableText;", shift = At.Shift.AFTER)
    )
    private static void saveWrittenFileToClipboard(NativeImage nativeImage, File screenshotFile, Consumer<Text> messageReceiver, CallbackInfo ci) throws IOException {
        if (!screenshotFile.getAbsolutePath().contains("/panorama/") && SnapperConfig.INSTANCE.copyTakenScreenshot.get()) {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(new ImageTransferable(ImageIO.read(screenshotFile)), null);
        }
    }

    @ModifyArg(method = "saveScreenshot(Ljava/io/File;Ljava/lang/String;Lnet/minecraft/client/gl/Framebuffer;Ljava/util/function/Consumer;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/ScreenshotRecorder;saveScreenshotInner(Ljava/io/File;Ljava/lang/String;Lnet/minecraft/client/gl/Framebuffer;Ljava/util/function/Consumer;)V", ordinal = 0))
    private static File getConfiguredGameDirectory(File gameDirectory) {
        File customScreenshotFolder = SnapperConfig.INSTANCE.customScreenshotFolder.get();

        if (SnapperConfig.INSTANCE.useCustomScreenshotFolder.get() && customScreenshotFolder.exists()) {
            return customScreenshotFolder;
        }
        return gameDirectory;
    }
}
