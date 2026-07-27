package dev.spiritstudios.snapper.mixin;

import dev.spiritstudios.snapper.util.clipboard.AWTClipboard;
import dev.spiritstudios.snapper.util.clipboard.Clipboard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    private void init(GameConfig gameConfig, CallbackInfo ci) {
        if (Clipboard.INSTANCE instanceof AWTClipboard) System.setProperty("java.awt.headless", "false");
    }
}
