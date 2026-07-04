package dev.spiritstudios.snapper.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.spiritstudios.snapper.Snapper;
import net.minecraft.client.renderer.CubeMap;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CubeMap.class)
public class CubeMapMixin {
    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/TextureManager;getTexture(Lnet/minecraft/resources/Identifier;)Lnet/minecraft/client/renderer/texture/AbstractTexture;"))
    private AbstractTexture getCustomTexture(TextureManager instance, Identifier location, Operation<AbstractTexture> original) {
        if (Snapper.CUBEMAP_TEXTURE.isBound()) {
            return original.call(instance, Snapper.CUBEMAP_TEXTURE.get());
        } else {
            return original.call(instance, location);
        }
    }
}
