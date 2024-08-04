package dev.spiritstudios.snapper.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.spiritstudios.snapper.Snapper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.File;
import java.util.function.Consumer;

import static net.minecraft.client.util.ScreenshotRecorder.takeScreenshot;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Redirect(
            method = "takePanorama",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/util/ScreenshotRecorder;saveScreenshot(Ljava/io/File;Ljava/lang/String;Lnet/minecraft/client/gl/Framebuffer;Ljava/util/function/Consumer;)V")
    )
    private void saveScreenshot(File gameDirectory, @Nullable String fileName, Framebuffer framebuffer, Consumer<Text> messageReceiver) {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> {
                savePanoramaInner(gameDirectory, fileName, framebuffer, messageReceiver);
            });
        } else {
            savePanoramaInner(gameDirectory, fileName, framebuffer, messageReceiver);
        }

    }
    @Unique
    private void savePanoramaInner(File gameDirectory, @Nullable String fileName, Framebuffer framebuffer, Consumer<Text> messageReceiver) {
        NativeImage nativeImage = takeScreenshot(framebuffer);
        File file = new File(gameDirectory, "screenshots/panorama");
        file.mkdir();
        File file2;
        if (fileName == null) {
            file2 = getScreenshotFilename(file);
        } else {
            file2 = new File(file, fileName);
        }

        Util.getIoWorkerExecutor().execute(() -> {
            try {
                nativeImage.writeTo(file2);
                Text text = Text.literal(file2.getName()).formatted(Formatting.UNDERLINE).styled((style) -> {
                    return style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file2.getAbsolutePath()));
                });
                messageReceiver.accept(Text.translatable("panorama.snapper.success", new Object[]{text}));
            } catch (Exception var7) {
                Exception exception = var7;
                Snapper.LOGGER.warn("Couldn't save panorama", exception);
                messageReceiver.accept(Text.translatable("panorama.snapper.failure", new Object[]{exception.getMessage()}));
            } finally {
                nativeImage.close();
            }

        });
    }
    @Unique
    private static File getScreenshotFilename(File directory) {
        String string = Util.getFormattedCurrentTime();
        int i = 1;

        while(true) {
            File file = new File(directory, string + (i == 1 ? "" : "_" + i) + ".png");
            if (!file.exists()) {
                return file;
            }

            ++i;
        }
    }
}
