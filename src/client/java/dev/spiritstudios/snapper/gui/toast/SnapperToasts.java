package dev.spiritstudios.snapper.gui.toast;

import dev.spiritstudios.snapper.SnapperComponents;
import dev.spiritstudios.snapper.SnapperConfig;
import dev.spiritstudios.snapper.SnapperKeyMappings;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class SnapperToasts {
    public static void configSaveFailure() {
        SnapperToast.push(
                SnapperToast.Type.FAILURE,
                Component.translatable("toast.snapper.config.failure.title"),
                SnapperComponents.CHECK_LOGS
        );
    }

    public static void screenshotCopySuccess() {
        SnapperToast.push(
                SnapperToast.Type.SCREENSHOT,
                Component.translatable("toast.snapper.screenshot.copy"),
                null
        );
    }

    public static void screenshotCopyFailure() {
        SnapperToast.push(
                SnapperToast.Type.FAILURE,
                Component.translatable("toast.snapper.screenshot.copy.failure"),
                SnapperComponents.CHECK_LOGS
        );
    }

    private static Component chooseScreenshotCreateDescription(Component name) {
        Minecraft minecraft = Minecraft.getInstance();

        // Lovely tree of decisions to decide what instructions make sense. <3 Lynn
        String inGameDeterminedDescription = minecraft.gui.screen() == null ?
                "toast.snapper.screenshot.created.description" :
                "toast.snapper.screenshot.created.description_in_menu";

        String copyDeterminedDescription = SnapperConfig.HOLDER.get().copyTakenScreenshot() ?
                "toast.snapper.screenshot.created.description_copy" :
                inGameDeterminedDescription;

        return Component.translatable(copyDeterminedDescription, name, SnapperKeyMappings.RECENT_SCREENSHOT_KEY.getTranslatedKeyMessage());
    }

    public static void screenshotCreateSuccess(Component fileName) {
        SnapperToast.push(
                SnapperToast.Type.SCREENSHOT,
                Component.translatable("toast.snapper.screenshot.created"),
                // Strip out embedded link to file for formatting consistency; you can't click it anyway
                chooseScreenshotCreateDescription(Component.literal(fileName.getString()))
        );
    }

    public static void panoramaCreateSuccess(Component fileName) {
        SnapperToast.push(
                SnapperToast.Type.PANORAMA,
                Component.translatable("toast.snapper.panorama.created"),
                chooseScreenshotCreateDescription(fileName)
        );
    }

    public static void imageUploadSuccess(Component url) {
        SnapperToast.push(
                SnapperToast.Type.UPLOAD,
                Component.translatable("toast.snapper.upload.success"),
                Component.translatable("toast.snapper.upload.success.description", url)
        );
    }

    private static void imageUploadFailure(Component description) {
        SnapperToast.push(
                SnapperToast.Type.FAILURE,
                Component.translatable("toast.snapper.upload.failure"),
                description
        );
    }

    public static void imageUploadFailureGeneric() {
        imageUploadFailure(SnapperComponents.CHECK_LOGS);
    }

    public static void imageUploadFailureOffline() {
        imageUploadFailure(Component.translatable("toast.snapper.upload.failure.offline"));
    }

    public static void imageUploadApiDisabledToast() {
        imageUploadFailure(Component.translatable("toast.snapper.upload.axolotlclient.api_disabled"));
    }
}
