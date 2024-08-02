package dev.spiritstudios.snapper.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.io.File;

@Mixin(MinecraftClient.class)
public interface HugeScreenshotInvoker {
    @Invoker("takeHugeScreenshot")
    Text callTakeHugeScreenshot(File gameDirectory, int unitWidth, int unitHeight, int width, int height);
}
