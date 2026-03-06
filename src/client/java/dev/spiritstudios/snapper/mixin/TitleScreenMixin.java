package dev.spiritstudios.snapper.mixin;

import dev.spiritstudios.snapper.Snapper;
import dev.spiritstudios.snapper.SnapperConfig;
import dev.spiritstudios.snapper.gui.screen.ScreenshotListScreen;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    @Unique
    private static final ResourceLocation SNAPPER_BUTTON_ICON = Snapper.id("screenshots/screenshot");

    protected TitleScreenMixin(Component title) {
        super(title);
    }

    @Inject(
            method = "init",
            at = @At("HEAD")
    )
    protected void init(CallbackInfo ci) {
        if (SnapperConfig.HOLDER.get().snapperButton().showOnTitleScreen()) {
			Objects.requireNonNull(minecraft);

			int y = this.height / 4 + 48;
			int spacingY = 24;

            this.addRenderableWidget(
                    SpriteIconButton.builder(
                            Component.translatable("button.snapper.screenshots"),
                            button -> this.minecraft.setScreen(new ScreenshotListScreen((TitleScreen) ((Object) this))),
                            true
                    ).width(20).sprite(SNAPPER_BUTTON_ICON, 15, 15).build()
            ).setPosition(this.width / 2 - 124, y + spacingY);
        }
    }
}
