package dev.spiritstudios.snapper.mixin;

import dev.spiritstudios.snapper.SnapperConfig;
import dev.spiritstudios.snapper.gui.ScreenshotScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.spiritstudios.snapper.Snapper.MODID;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    @Unique
    private static final Identifier SNAPPER_BUTTON_ICON = Identifier.of(MODID, "screenshots/screenshot");

    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(
            method = "init",
            at = @At("HEAD")
    )
    protected void init(CallbackInfo ci) {
        if (this.client == null) return;
        int y = this.height / 4 + 48;
        int spacingY = 24;

        if (SnapperConfig.INSTANCE.showSnapperTitleScreen.get()) {
            this.addDrawableChild(
                    TextIconButtonWidget.builder(
                            Text.translatable("button.snapper.screenshots"),
                            button -> this.client.setScreen(new ScreenshotScreen((TitleScreen) ((Object) this))),
                            true
                    ).width(20).texture(SNAPPER_BUTTON_ICON, 15, 15).build()
            ).setPosition(this.width / 2 - 124, y + spacingY);
        }
    }
}
