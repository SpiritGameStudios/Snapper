package dev.spiritstudios.snapper.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import dev.spiritstudios.snapper.Snapper;
import dev.spiritstudios.snapper.SnapperConfig;
import dev.spiritstudios.snapper.gui.screen.GalleryScreen;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PauseScreen.class)
public abstract class PauseScreenMixin extends Screen {
    protected PauseScreenMixin(Component title) {
        super(title);
    }

    @Definition(id = "iconButtonRow", local = @Local(type = LinearLayout.class, name = "iconButtonRow"))
    @Definition(id = "addChild", method = "Lnet/minecraft/client/gui/layouts/LinearLayout;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;)Lnet/minecraft/client/gui/layouts/LayoutElement;")
    @Definition(id = "playerReportingButton", local = @Local(type = SpriteIconButton.class, name = "playerReportingButton"))
    @Expression("iconButtonRow.addChild(playerReportingButton)")
    @Inject(
            method = "createPauseMenu",
            at = @At(value = "MIXINEXTRAS:EXPRESSION", shift = At.Shift.AFTER)
    )
    protected void addSnapperButton(CallbackInfo ci, @Local(name = "iconButtonRow") LinearLayout iconButtonRow) {
        if (SnapperConfig.HOLDER.get().snapperButton().showInGameMenu()) {
            iconButtonRow.addChild(Snapper.createSnapperButton(20, _ -> {
                this.minecraft.gui.setScreen(new GalleryScreen(this));
            }));
        }
        if (SnapperConfig.HOLDER.get().showScreenshotHelper()) {
            iconButtonRow.addChild(SpriteIconButton.builder(
                            Component.translatable("button.snapper.helper.screenshot"),
                            _ -> {
                                this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(Snapper.SHUTTER, 1.0F));
                                this.minecraft.gui.setScreen(null);
                                Screenshot.grab(this.minecraft, false);
                            },
                            true
                    )
                    .width(20)
                    .sprite(Snapper.id("screenshots/helper"), 15, 15)
                    .tooltip(Component.translatable("button.snapper.helper.screenshot"))
                    .narration(_ -> Component.translatable("button.snapper.helper.screenshot"))
                    .build());
        }
    }
}
