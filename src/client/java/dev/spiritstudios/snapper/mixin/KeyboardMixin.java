package dev.spiritstudios.snapper.mixin;

import dev.spiritstudios.snapper.SnapperConfig;
import dev.spiritstudios.snapper.SnapperKeybindings;
import dev.spiritstudios.snapper.gui.toast.SnapperToast;
import dev.spiritstudios.snapper.util.SnapperUtil;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Keyboard.class)
public abstract class KeyboardMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    /**
     * @author CallMeEcho & WorldWidePixel
     * @reason Change message logic to show a toast instead of chat message
     */
    @Overwrite
    private void method_1464(Text text) {
        // Lovely tree of decisions to decide what instructions make sense. <3 Lynn
        String inGameDeterminedDescription = client.currentScreen == null ? "toast.snapper.screenshot.created.description"
                : "toast.snapper.screenshot.created.description_in_menu";
        String copyDeterminedDescription = SnapperConfig.INSTANCE.copyTakenScreenshot.get() ?
                "toast.snapper.screenshot.created.description_copy" : inGameDeterminedDescription;

        SnapperUtil.toast(
                SnapperToast.Type.SCREENSHOT,
                Text.translatable("toast.snapper.screenshot.created"),
                Text.translatable(copyDeterminedDescription, text, SnapperKeybindings.RECENT_SCREENSHOT_KEY.getBoundKeyLocalizedText())
        );
    }
}
