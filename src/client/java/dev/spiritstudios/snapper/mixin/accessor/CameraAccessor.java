package dev.spiritstudios.snapper.mixin.accessor;

import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Camera.class)
public interface CameraAccessor {
    @Invoker
    void invokeSetRotation(float yaw, float pitch);
}
