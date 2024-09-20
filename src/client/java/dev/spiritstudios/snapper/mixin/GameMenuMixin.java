package dev.spiritstudios.snapper.mixin;

import dev.spiritstudios.snapper.SnapperConfig;
import dev.spiritstudios.snapper.gui.SnapperScreen;
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
            method = "init",
            at = @At("TAIL")
    )
    protected void head(CallbackInfo ci) {
        int displayPosition = SnapperConfig.INSTANCE.gameMenuDisplayPosition.get();
        int displayX = width / 2 - 130;
        int displayY = height / 4 + 32;
        switch (displayPosition) {
            case 2: displayY = height / 4 + 16; break;
            case 3: displayY = height / 4 + 0; break;
            case 4: displayX = width / 2 + 120; break;
            case 5: displayX = width / 2 + 120; displayY = height / 4 + 16; break;
            case 6: displayX = width / 2 + 120; displayY = height / 4 + 0; break;
        }
        if (displayPosition > 0) {
            this.addDrawableChild(
                    TextIconButtonWidget.builder(
                            Text.translatable("button.snapper.screenshots"),
                            button -> {
                                if (this.client == null)
                                    return;

                                this.client.setScreen(new SnapperScreen(new GameMenuScreen(true)));
                            },
                            true
                    ).width(20).texture(SNAPPER_BUTTON_ICON, 15, 15).build()
            ).setPosition(displayX, displayY);
        }

    }
}
