package dev.spiritstudios.snapper.mixin;

import dev.spiritstudios.snapper.Snapper;
import dev.spiritstudios.snapper.SnapperConfig;
import dev.spiritstudios.snapper.gui.screen.ScreenshotListScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PauseScreen.class)
public abstract class PauseScreenMixin extends Screen {
    protected PauseScreenMixin(Component title) {
        super(title);
    }

    @Unique
    private static final ResourceLocation SNAPPER_BUTTON_ICON = Snapper.id("screenshots/screenshot");

    @Inject(
            method = "init",
            at = @At("TAIL")
    )
    protected void initWidgets(CallbackInfo ci) {
        if (SnapperConfig.HOLDER.get().snapperButton().showInGameMenu()) {
            this.addRenderableWidget(
                    SpriteIconButton.builder(
                            Component.translatable("button.snapper.screenshots"),
                            button -> {
                                Minecraft.getInstance().setScreen(new ScreenshotListScreen(new PauseScreen(true)));
                            },
                            true
                    ).width(20).sprite(SNAPPER_BUTTON_ICON, 15, 15).build()
            ).setPosition(this.width / 2 - 130, height / 4 + 32);
        }
    }
}
