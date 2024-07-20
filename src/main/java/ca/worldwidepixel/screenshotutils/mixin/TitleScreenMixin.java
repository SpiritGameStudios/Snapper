package ca.worldwidepixel.screenshotutils.mixin;

import ca.worldwidepixel.screenshotutils.screen.screenshot.ScreenshotScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.PlayerSkinWidget;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.util.SkinTextures;
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
        this.addDrawableChild(
                ButtonWidget.builder(Text.translatable("menu.screenshotutils.screenshotmenu"), button -> this.client.setScreen(new ScreenshotScreen()))
                        .dimensions(this.width - 100, 0, 100, 20)
                        .build()
        );
    }
}
