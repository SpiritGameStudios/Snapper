package dev.spiritstudios.snapper.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.spiritstudios.snapper.gui.screen.PanoramaViewerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Hud.class)
public class HudMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Definition(id = "LevelLoadingScreen", type = LevelLoadingScreen.class)
    @Definition(id = "minecraft", field = "Lnet/minecraft/client/gui/Hud;minecraft:Lnet/minecraft/client/Minecraft;")
    @Definition(id = "gui", field = "Lnet/minecraft/client/Minecraft;gui:Lnet/minecraft/client/gui/Gui;")
    @Definition(id = "screen", method = "Lnet/minecraft/client/gui/Gui;screen()Lnet/minecraft/client/gui/screens/Screen;")
    @Expression("this.minecraft.gui.screen() instanceof LevelLoadingScreen")
    @ModifyExpressionValue(method = "extractRenderState", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    private boolean cancelRenderingHudInPanoramaScreen(boolean original) {
        return original || this.minecraft.gui.screen() instanceof PanoramaViewerScreen;
    }
}
