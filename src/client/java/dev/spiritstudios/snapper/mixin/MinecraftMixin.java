package dev.spiritstudios.snapper.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import com.mojang.blaze3d.pipeline.RenderTarget;
import dev.spiritstudios.snapper.SnapperConfig;
import dev.spiritstudios.snapper.mixin.accessor.CameraAccessor;
import dev.spiritstudios.snapper.util.clipboard.AWTClipboard;
import dev.spiritstudios.snapper.util.clipboard.Clipboard;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.util.function.Consumer;

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
