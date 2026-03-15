package dev.spiritstudios.snapper.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Inject(method = "setup", at = @At("HEAD"), cancellable = true)
    private void blockUpdateDuringPanoramaRender(
            Level level, Entity entity, boolean detached, boolean mirror, float partialTickTime, CallbackInfo ci
    ) {
        if (Minecraft.getInstance().gameRenderer.isPanoramicMode() && detached) ci.cancel();
    }
}
