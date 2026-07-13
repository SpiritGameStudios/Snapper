package dev.spiritstudios.snapper.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import dev.spiritstudios.snapper.Snapper;
import dev.spiritstudios.snapper.SnapperConfig;
import dev.spiritstudios.snapper.gui.screen.GalleryScreen;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    @Shadow
    protected abstract int getHorizontalPosition(int currentButton, int numberOfButtons, int buttonWidth);

    protected TitleScreenMixin(Component title) {
        super(title);
    }

    @Definition(id = "numberOfButtons", local = @Local(type = int.class, name = "numberOfButtons"))
    @Expression("numberOfButtons = ?")
    @Inject(method = "init", at = @At(value = "MIXINEXTRAS:EXPRESSION", shift = At.Shift.AFTER))
    private void increaseNumberOfButtons(CallbackInfo ci, @Local(name = "numberOfButtons") LocalIntRef numberOfButtons) {
        if (SnapperConfig.HOLDER.get().snapperButton().showOnTitleScreen()) {
            numberOfButtons.set(numberOfButtons.get() + 1);
        }
    }

    @WrapOperation(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/TitleScreen;getHorizontalPosition(III)I"))
    private int unInlineNumberOfButtons(TitleScreen instance, int currentButton, int ignored, int buttonWidth, Operation<Integer> original, @Local(name = "numberOfButtons") int numberOfButtons) {
        return original.call(instance, currentButton, numberOfButtons, buttonWidth);
    }

    @Definition(id = "accessibility", local = @Local(type = SpriteIconButton.class, name = "accessibility"))
    @Definition(id = "setPosition", method = "Lnet/minecraft/client/gui/components/SpriteIconButton;setPosition(II)V")
    @Expression("accessibility.setPosition(?, ?)")
    @Inject(method = "init", at = @At(value = "MIXINEXTRAS:EXPRESSION", shift = At.Shift.AFTER))
    private void addButton(
            CallbackInfo ci,
            @Local(name = "currentButton") LocalIntRef currentButton,
            @Local(name = "numberOfButtons") int numberOfButtons,
            @Local(name = "topPos") int topPos
    ) {
        if (SnapperConfig.HOLDER.get().snapperButton().showOnTitleScreen()) {
            SpriteIconButton snapperButton = this.addRenderableWidget(Snapper.createSnapperButton(20, _ -> {
                this.minecraft.gui.setScreen(new GalleryScreen(this));
            }));

            var buttonIndex = currentButton.get() + 1;
            currentButton.set(buttonIndex);

            snapperButton.setPosition(this.getHorizontalPosition(buttonIndex, numberOfButtons, 20), topPos);
        }
    }
}
