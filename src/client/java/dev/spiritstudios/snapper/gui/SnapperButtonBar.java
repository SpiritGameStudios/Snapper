package dev.spiritstudios.snapper.gui;

import dev.spiritstudios.snapper.Snapper;
import dev.spiritstudios.snapper.gui.screen.ConfigScreen;
import dev.spiritstudios.snapper.gui.screen.PanoramaViewerScreen;
import dev.spiritstudios.snapper.gui.screen.ScreenshotRenameScreen;
import dev.spiritstudios.snapper.gui.widget.ViewModeButton;
import dev.spiritstudios.snapper.util.PlatformHelper;
import dev.spiritstudios.snapper.util.ScreenshotActions;
import dev.spiritstudios.snapper.util.ScreenshotTexture;
import dev.spiritstudios.snapper.util.SnapperUtil;
import dev.spiritstudios.snapper.util.uploading.ScreenshotUploading;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.function.Supplier;

public class SnapperButtonBar {
    private static final Identifier PANORAMA_BUTTON_ICON = Snapper.id("screenshots/panorama");
    private static final Identifier PANORAMA_BUTTON_DISABLED_ICON = Snapper.id("screenshots/panorama_disabled");

    private static final Identifier SETTINGS_ICON = Snapper.id("screenshots/settings");
    private static final Identifier RELOAD_ICON = Snapper.id("screenshots/reset");

    public final Button deleteButton;
    public final Button renameButton;
    public final Button copyButton;
    public final Button openButton;
    public final Button uploadButton;

    public SnapperButtonBar(
            Screen screen,
            Screen postFlowScreen,
            HeaderAndFooterLayout layout,
            Supplier<@Nullable ScreenshotTexture> getTexture,
            boolean enablePanoramaButton,
            @Nullable Runnable toggleGrid,
            @Nullable Runnable reload
    ) {
        Minecraft minecraft = Minecraft.getInstance();

        final int hSpacing = 4;

        final int buttonWidth = 74;
        final int bottomButtonWidth = 100;

        LinearLayout vertical = layout.addToFooter(LinearLayout.vertical().spacing(4));
        vertical.defaultCellSetting().alignHorizontallyCenter();

        LinearLayout topRow = vertical.addChild(LinearLayout.horizontal().spacing(hSpacing));
        LinearLayout bottomRow = vertical.addChild(LinearLayout.horizontal().spacing(hSpacing));

        bottomRow.addChild(SpriteIconButton.builder(
                Component.translatable("config.snapper.title"),
                _ -> minecraft.gui.setScreen(new ConfigScreen(screen)),
                true
        ).width(20).sprite(SETTINGS_ICON, 15, 15).build());

        bottomRow.addChild(Button.builder(
                Component.translatable("button.snapper.folder"),
                _ -> Util.getPlatform().openPath(SnapperUtil.getConfiguredScreenshotDirectory())
        ).width(bottomButtonWidth).build());

        this.openButton = bottomRow.addChild(Button.builder(
                Component.translatable("button.snapper.open"),
                _ -> {
                    ScreenshotTexture selected = getTexture.get();

                    if (selected != null) {
                        Util.getPlatform().openPath(selected.path);
                    }
                }
        ).width(bottomButtonWidth).build());

        bottomRow.addChild(Button.builder(
                CommonComponents.GUI_DONE,
                _ -> screen.onClose()
        ).width(bottomButtonWidth).build());
        Path panoramaDir = SnapperUtil.getConfiguredScreenshotDirectory().resolve("panorama");

        boolean hasPanorama = enablePanoramaButton && SnapperUtil.panoramaPresent(panoramaDir);
        SpriteIconButton panoramaButton = bottomRow.addChild(
                SpriteIconButton.builder(
                        Component.translatable("button.snapper.screenshots"),
                        _ -> minecraft.gui.setScreen(new PanoramaViewerScreen(Component.translatable("menu.snapper.panorama").getString(), screen)),
                        true
                ).width(20).sprite(hasPanorama ? PANORAMA_BUTTON_ICON : PANORAMA_BUTTON_DISABLED_ICON, 15, 15).build());

        panoramaButton.active = hasPanorama;

        if (enablePanoramaButton) {
            panoramaButton.setTooltip(Tooltip.create(Component.translatable(hasPanorama ?
                    "button.snapper.panorama.tooltip" :
                    "text.snapper.panorama_encourage")));
        }

        ViewModeButton viewModeButton = new ViewModeButton(
                _ -> {
                    if (toggleGrid != null) toggleGrid.run();
                },
                null
        );

        viewModeButton.active = toggleGrid != null;

        topRow.addChild(viewModeButton);

        this.deleteButton = topRow.addChild(Button.builder(
                Component.translatable("button.snapper.delete"),
                _ -> {
                    ScreenshotTexture selected = getTexture.get();

                    if (selected != null) {
                        ScreenshotActions.deleteScreenshot(selected.path, postFlowScreen);
                    }
                }
        ).width(buttonWidth).build());

        this.renameButton = topRow.addChild(Button.builder(
                Component.translatable("button.snapper.rename"),
                _ -> {
                    ScreenshotTexture selected = getTexture.get();

                    if (selected != null) {
                        minecraft.gui.setScreen(new ScreenshotRenameScreen(selected.path, postFlowScreen));
                    }
                }
        ).width(buttonWidth).build());

        this.copyButton = topRow.addChild(Button.builder(
                Component.translatable("button.snapper.copy"),
                _ -> {
                    ScreenshotTexture selected = getTexture.get();

                    if (selected != null) {
                        PlatformHelper.INSTANCE.copyScreenshot(selected.path);
                    }
                }
        ).width(buttonWidth).build());

        this.uploadButton = topRow.addChild(Button.builder(Component.translatable("button.snapper.upload"), button -> {
            ScreenshotTexture selected = getTexture.get();

            if (selected == null) return;

            button.active = false;
            ScreenshotUploading.upload(selected.path)
                    .thenRun(() -> button.active = true);
        }).width(buttonWidth).build());

        if (SnapperUtil.isOfflineAccount()) {
            this.uploadButton.active = false;
            this.uploadButton.setTooltip(Tooltip.create(Component.translatable("button.snapper.upload.tooltip")));
        }

        var reloadButton = SpriteIconButton.builder(
                Component.translatable("button.snapper.reload"),
                _ -> {
                    if (reload != null) reload.run();
                },
                true
        ).width(20).sprite(RELOAD_ICON, 15, 15).build();

        reloadButton.active = reload != null;

        topRow.addChild(reloadButton);
    }
}
