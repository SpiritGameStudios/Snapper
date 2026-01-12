package dev.spiritstudios.snapper.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderTarget;
import dev.spiritstudios.snapper.SnapperConfig;
import dev.spiritstudios.snapper.SnapperKeybindings;
import dev.spiritstudios.snapper.gui.toast.SnapperToast;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.io.File;
import java.util.function.Consumer;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @WrapOperation(method = "keyPress", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Screenshot;grab(Ljava/io/File;Lcom/mojang/blaze3d/pipeline/RenderTarget;Ljava/util/function/Consumer;)V"))
    private void showDebugChat(File gameDirectory, RenderTarget renderTarget, Consumer<Component> messageConsumer, Operation<Void> original) {
        original.call(
                gameDirectory,
                renderTarget,
                (Consumer<Component>) message -> {
                    // Execute on the render thread.
                    Minecraft.getInstance().execute(() -> {
                        // Lovely tree of decisions to decide what instructions make sense. <3 Lynn
                        String inGameDeterminedDescription = minecraft.screen == null ? "toast.snapper.screenshot.created.description"
                                : "toast.snapper.screenshot.created.description_in_menu";
                        String copyDeterminedDescription = SnapperConfig.HOLDER.get().copyTakenScreenshot() ?
                                "toast.snapper.screenshot.created.description_copy" : inGameDeterminedDescription;

                        SnapperToast.push(
                                SnapperToast.Type.SCREENSHOT,
                                Component.translatable("toast.snapper.screenshot.created"),
                                Component.translatable(copyDeterminedDescription, message, SnapperKeybindings.RECENT_SCREENSHOT_KEY.getTranslatedKeyMessage())
                        );
                    });
                }
        );
    }
}
