package dev.spiritstudios.snapper.mixin;

import dev.spiritstudios.snapper.SnapperConfig;
import dev.spiritstudios.snapper.gui.ScreenshotScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.spiritstudios.snapper.Snapper.MODID;

@Mixin(GameMenuScreen.class)
public class GameMenuMixin extends Screen {
    protected GameMenuMixin(Text title) {
        super(title);
    }

    @Unique
    private static final Identifier SNAPPER_BUTTON_ICON = Identifier.of(MODID, "screenshots/screenshot");

    @Inject(
            method = "initWidgets",
            at = @At("TAIL")
    )
    protected void initWidgets(CallbackInfo ci) {
        if (SnapperConfig.INSTANCE.showSnapperGameMenu.get()) {
            this.addDrawableChild(
                    TextIconButtonWidget.builder(
                            Text.translatable("button.snapper.screenshots"),
                            button -> {
                                if (this.client == null)
                                    return;

                                this.client.setScreen(new ScreenshotScreen(new GameMenuScreen(true)));
                            },
                            true
                    ).width(20).texture(SNAPPER_BUTTON_ICON, 15, 15).build()
            ).setPosition(this.width / 2 - 130, height / 4 + 32);
        }
    }
}
