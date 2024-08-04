package dev.spiritstudios.snapper.mixin;

import dev.spiritstudios.snapper.gui.ScreenshotScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(
            method = "init",
            at = @At("HEAD")
    )
    protected void init(CallbackInfo ci) {
        if (this.client == null) return;

        this.addDrawableChild(
                ButtonWidget.builder(Text.translatable("menu.snapper.screenshotmenu"), button -> this.client.setScreen(new ScreenshotScreen(this)))
                        .dimensions(this.width - 100, 0, 100, 20)
                        .build()
        );
    }
}
