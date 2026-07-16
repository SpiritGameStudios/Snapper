package dev.spiritstudios.snapper.gui;

import dev.spiritstudios.snapper.Snapper;
import dev.spiritstudios.snapper.SnapperConfig;
import dev.spiritstudios.snapper.gui.screen.ConfigScreen;
import dev.spiritstudios.snapper.gui.screen.ScreenshotRenameScreen;
import dev.spiritstudios.snapper.gui.widget.ViewModeButton;
import dev.spiritstudios.snapper.render.texture.GalleryTexture;
import dev.spiritstudios.snapper.util.ScreenshotActions;
import dev.spiritstudios.snapper.util.SnapperUtil;
import dev.spiritstudios.snapper.util.uploading.AxolotlClientApi;
import dev.spiritstudios.snapper.util.uploading.ScreenshotUploading;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

public class SnapperButtonBar {
    private static final Identifier SETTINGS_ICON = Snapper.id("screenshots/settings");
    private static final Identifier RELOAD_ICON = Snapper.id("screenshots/reset");

    public final LinearLayout layout;

    public final Button deleteButton;
    public final Button renameButton;
    public final Button copyButton;
    public final Button openButton;
    public final Button uploadButton;

    public SnapperButtonBar(
            Screen screen,
            Screen postFlowScreen,
            Supplier<@Nullable GalleryTexture> getTexture,
            @Nullable Runnable toggleGrid,
            @Nullable Runnable reload
    ) {
        Minecraft minecraft = Minecraft.getInstance();

        final int hSpacing = 4;

        final int iconSize = 20;
        final int iconSpriteSize = 15;
        final int buttonWidth = 74;
        final int bottomButtonWidth = 100;


        LinearLayout vertical = LinearLayout.vertical().spacing(4);
        vertical.defaultCellSetting().alignHorizontallyCenter();

        this.layout = vertical;

        LinearLayout topRow = vertical.addChild(LinearLayout.horizontal().spacing(hSpacing));
        LinearLayout bottomRow = vertical.addChild(LinearLayout.horizontal().spacing(hSpacing));

        bottomRow.addChild(SpriteIconButton.builder(
                Component.translatable("config.snapper.title"),
                _ -> minecraft.gui.setScreen(new ConfigScreen(screen)),
                true
        ).width(iconSize).sprite(SETTINGS_ICON, iconSpriteSize, iconSpriteSize).build());

        bottomRow.addChild(Button.builder(
                Component.translatable("button.snapper.folder"),
                _ -> Util.getPlatform().openPath(ScreenshotActions.getScreenshotDirectory())
        ).width(bottomButtonWidth).build());

        this.openButton = bottomRow.addChild(Button.builder(
                Component.translatable("button.snapper.open"),
                _ -> {
                    GalleryTexture selected = getTexture.get();

                    if (selected != null) {
                        Util.getPlatform().openPath(selected.path);
                    }
                }
        ).width(bottomButtonWidth).build());

        bottomRow.addChild(Button.builder(
                CommonComponents.GUI_DONE,
                _ -> screen.onClose()
        ).width(bottomButtonWidth).build());

        var reloadButton = SpriteIconButton.builder(
                Component.translatable("button.snapper.reload"),
                _ -> {
                    if (reload != null) reload.run();
                },
                true
        ).width(iconSize).sprite(RELOAD_ICON, iconSpriteSize, iconSpriteSize).build();
        reloadButton.active = reload != null;
        bottomRow.addChild(reloadButton);

        ViewModeButton viewModeButton = new ViewModeButton(
                _ -> {
                    if (toggleGrid != null) toggleGrid.run();
                },
                null
        );

        viewModeButton.active = toggleGrid != null;

        topRow.addChild(viewModeButton);

        this.copyButton = topRow.addChild(Button.builder(
                Component.translatable("button.snapper.copy"),
                _ -> {
                    GalleryTexture selected = getTexture.get();

                    if (selected != null) {
                        ScreenshotActions.copyScreenshot(selected.path, true);
                    }
                }
        ).width(buttonWidth).build());

        this.uploadButton = topRow.addChild(Button.builder(Component.translatable("button.snapper.upload"), button -> {
            GalleryTexture selected = getTexture.get();

            if (selected == null) return;

            setUploadButtonActive(false);
            ScreenshotUploading.upload(selected.path, true).thenRun(() -> setUploadButtonActive(true));
        }).width(buttonWidth).build());

        this.setUploadButtonActive(true);

        this.renameButton = topRow.addChild(Button.builder(
                Component.translatable("button.snapper.rename"),
                _ -> {
                    GalleryTexture selected = getTexture.get();

                    if (selected != null) {
                        minecraft.gui.setScreen(new ScreenshotRenameScreen(selected.path, postFlowScreen));
                    }
                }
        ).width(buttonWidth).build());

        this.deleteButton = topRow.addChild(Button.builder(
                Component.translatable("button.snapper.delete"),
                _ -> {
                    GalleryTexture selected = getTexture.get();

                    if (selected != null) {
                        ScreenshotActions.deleteScreenshot(selected.path, postFlowScreen);
                    }
                }
        ).width(buttonWidth).build());

        topRow.addChild(new SpacerElement(iconSize, iconSize));
    }

    public void setUploadButtonActive(boolean value) {
        if (value) {
            if (SnapperUtil.isOfflineAccount()) {
                this.uploadButton.active = false;
                this.uploadButton.setTooltip(Tooltip.create(Component.translatable("button.snapper.upload.tooltip.offline")));
            } else if (SnapperConfig.HOLDER.get().axolotlClient().termsStatus() == AxolotlClientApi.TermsAcceptance.DENIED) {
                this.uploadButton.active = false;
                this.uploadButton.setTooltip(Tooltip.create(Component.translatable("button.snapper.upload.tooltip.tos")));
            } else {
                this.uploadButton.active = true;
                this.uploadButton.setTooltip(null);
            }
        } else {
            this.uploadButton.active = false;
            this.uploadButton.setTooltip(null);
        }
    }
}
