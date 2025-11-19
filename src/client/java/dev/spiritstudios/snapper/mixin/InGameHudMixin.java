package dev.spiritstudios.snapper.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.spiritstudios.snapper.gui.screen.PanoramaViewerScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Shadow
    @Final
    private MinecraftClient client;

    @Definition(id = "DownloadingTerrainScreen", type = DownloadingTerrainScreen.class)
    @Definition(id = "client", field = "Lnet/minecraft/client/gui/hud/InGameHud;client:Lnet/minecraft/client/MinecraftClient;")
    @Definition(id = "currentScreen", field = "Lnet/minecraft/client/MinecraftClient;currentScreen:Lnet/minecraft/client/gui/screen/Screen;")
    @Expression("(this.client.currentScreen instanceof DownloadingTerrainScreen)")
    @ModifyExpressionValue(method = "render", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    private boolean cancelRenderingHudInPanoramaScreen(boolean original) {
        return original || client.currentScreen instanceof PanoramaViewerScreen;
    }
}
