package dev.spiritstudios.snapper.gui.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.serialization.Codec;
import dev.spiritstudios.snapper.Snapper;
import dev.spiritstudios.snapper.SnapperConfig;
import dev.spiritstudios.snapper.gui.toast.SnapperToast;
import dev.spiritstudios.snapper.gui.widget.ScreenshotListWidget;
import dev.spiritstudios.snapper.gui.widget.ScreenshotsWidget;
import dev.spiritstudios.snapper.gui.widget.ViewModeButton;
import dev.spiritstudios.snapper.util.PlatformHelper;
import dev.spiritstudios.snapper.util.ScreenshotActions;
import dev.spiritstudios.snapper.util.SnapperUtil;
import dev.spiritstudios.snapper.util.uploading.ScreenshotUploading;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.nio.file.Path;

public class ScreenshotListScreen extends Screen implements ReloadableScreen {
    private static final Identifier PANORAMA_BUTTON_ICON = Snapper.id("screenshots/panorama");
    private static final Identifier PANORAMA_BUTTON_DISABLED_ICON = Snapper.id("screenshots/panorama_disabled");

    private static final Identifier SETTINGS_ICON = Snapper.id("screenshots/settings");
    private static final Identifier RELOAD_ICON = Snapper.id("screenshots/reset");

    private final Screen parent;
    private final boolean isOffline;

    private ScreenshotsWidget screenshots = null;

    private Button deleteButton;
    private Button renameButton;
    private Button copyButton;
    private Button openButton;
    private Button uploadButton;

    private @Nullable ScreenshotListWidget.ScreenshotEntry selectedScreenshot = null;

    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, 33, 60);

    public ScreenshotListScreen(Screen parent) {
        super(Component.translatable("menu.snapper.screenshot_menu"));
        this.parent = parent;
        this.isOffline = SnapperUtil.isOfflineAccount();
        this.recreateList();
    }

    public synchronized void recreateList() {
        if (screenshots != null) {
            this.removeWidget(screenshots);
        }

        screenshots = this.addRenderableWidget(ScreenshotsWidget.create(
                minecraft,
                width,
                height - 48 - 68,
                48,
                screenshots,
                this
        ));

        repositionElements();
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
        screenshots.updateSize(
                width,
                layout
        );
    }

    @Override
    protected void init() {
        this.layout.addTitleHeader(this.title, this.font);

        final int hSpacing = 4;

        final int buttonWidth = 74;
        final int bottomButtonWidth = 100;

        LinearLayout vertical = this.layout.addToFooter(LinearLayout.vertical().spacing(4));
        vertical.defaultCellSetting().alignHorizontallyCenter();

        LinearLayout topRow = vertical.addChild(LinearLayout.horizontal().spacing(hSpacing));
        LinearLayout bottomRow = vertical.addChild(LinearLayout.horizontal().spacing(hSpacing));

        bottomRow.addChild(SpriteIconButton.builder(
                Component.translatable("config.snapper.title"),
                button -> this.minecraft.setScreen(
                        new ConfigScreen(this)),
                true
        ).width(20).sprite(SETTINGS_ICON, 15, 15).build());

        bottomRow.addChild(Button.builder(
                Component.translatable("button.snapper.folder"),
                button -> Util.getPlatform().openPath(SnapperUtil.getConfiguredScreenshotDirectory())
        ).width(bottomButtonWidth).build());

        this.openButton = bottomRow.addChild(Button.builder(
                Component.translatable("button.snapper.open"),
                button -> {
                    if (selectedScreenshot != null) {
                        Util.getPlatform().openPath(selectedScreenshot.texture.path);
                    }
                }
        ).width(bottomButtonWidth).build());

        bottomRow.addChild(Button.builder(
                CommonComponents.GUI_DONE,
                _ -> this.onClose()
        ).width(bottomButtonWidth).build());
        Path panoramaDir = SnapperUtil.getConfiguredScreenshotDirectory().resolve("panorama");
        boolean hasPanorama = SnapperUtil.panoramaPresent(panoramaDir);

        SpriteIconButton panoramaButton = bottomRow.addChild(
                SpriteIconButton.builder(
                        Component.translatable("button.snapper.screenshots"),
                        button -> this.minecraft.setScreen(new PanoramaViewerScreen(Component.translatable("menu.snapper.panorama").getString(), this)),
                        true
                ).width(20).sprite(hasPanorama ? PANORAMA_BUTTON_ICON : PANORAMA_BUTTON_DISABLED_ICON, 15, 15).build());

        panoramaButton.active = hasPanorama;

        panoramaButton.setTooltip(Tooltip.create(Component.translatable(hasPanorama ?
                "button.snapper.panorama.tooltip" :
                "text.snapper.panorama_encourage")));

        topRow.addChild(new ViewModeButton(
                button -> this.toggleGrid(),
                null
        ));

        this.deleteButton = topRow.addChild(Button.builder(
                Component.translatable("button.snapper.delete"),
                button -> {
                    if (selectedScreenshot != null) {
                        ScreenshotActions.deleteScreenshot(selectedScreenshot.texture.path, this);
                    }
                }
        ).width(buttonWidth).build());

        this.renameButton = topRow.addChild(Button.builder(
                Component.translatable("button.snapper.rename"),
                _ -> {
                    if (this.selectedScreenshot != null) {
                        minecraft.setScreen(new ScreenshotRenameScreen(this.selectedScreenshot.texture.path, this));
                    }
                }
        ).width(buttonWidth).build());

        this.copyButton = topRow.addChild(Button.builder(
                Component.translatable("button.snapper.copy"),
                button -> {
                    if (selectedScreenshot != null) {
                        PlatformHelper.INSTANCE.copyScreenshot(selectedScreenshot.texture.path);
                    }
                }
        ).width(buttonWidth).build());

        this.uploadButton = topRow.addChild(Button.builder(Component.translatable("button.snapper.upload"), button -> {
            if (selectedScreenshot == null) return;

            button.active = false;
            ScreenshotUploading.upload(selectedScreenshot.texture.path)
                    .thenRun(() -> button.active = true);
        }).width(buttonWidth).build());

        if (isOffline) {
            this.uploadButton.setTooltip(Tooltip.create(Component.translatable("button.snapper.upload.tooltip")));
        }

        topRow.addChild(
                SpriteIconButton.builder(
                        Component.translatable("button.snapper.reload"),
                        button -> this.reload(),
                        true
                ).width(20).sprite(RELOAD_ICON, 15, 15).build());

        this.imageSelected(selectedScreenshot);

        this.layout.visitWidgets(this::addRenderableWidget);

        this.repositionElements();
    }

    public ScreenshotsWidget getScreenshots() {
        return screenshots;
    }

    @Override
    public void onClose() {
        super.onClose();
        screenshots.clearEntries();
    }

    public void imageSelected(@Nullable ScreenshotListWidget.ScreenshotEntry screenshot) {
        boolean hasScreenshot = screenshot != null;
        this.copyButton.active = hasScreenshot;
        this.deleteButton.active = hasScreenshot;
        this.openButton.active = hasScreenshot;
        this.renameButton.active = hasScreenshot;
        this.selectedScreenshot = screenshot;
        this.uploadButton.active = !isOffline && hasScreenshot;
    }

    public void toggleGrid() {
        SnapperConfig.edit(m -> m.viewMode = SnapperConfig.HOLDER.get().viewMode() == ViewMode.GRID ? ViewMode.LIST : ViewMode.GRID);

        recreateList();
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        if (super.keyPressed(input)) {
            return true;
        }

        if (input.key() == InputConstants.KEY_F5) {
            minecraft.setScreen(new ScreenshotListScreen(this.parent));
            return true;
        }

        if (selectedScreenshot == null) return false;

        if ((input.modifiers() & InputConstants.MOD_CONTROL) != 0 && input.key() == InputConstants.KEY_C) {
            PlatformHelper.INSTANCE.copyScreenshot(selectedScreenshot.texture.path);
            SnapperToast.push(SnapperToast.Type.SCREENSHOT, Component.translatable("toast.snapper.screenshot.copy"), null);
            return true;
        }

        if (input.key() == InputConstants.KEY_RETURN) {
            minecraft.setScreen(new ScreenshotViewerScreen(selectedScreenshot.texture, this));
            return true;
        }

        return false;
    }

    @Override
    public void reload() {
        screenshots.reload();
    }

    public enum ViewMode implements StringRepresentable {
        LIST("list"),
        GRID("grid");

        public static final Codec<ViewMode> CODEC = StringRepresentable.fromEnum(ViewMode::values);

        private final String name;

        ViewMode(String name) {
            this.name = name;
        }

        @Override
        public @NonNull String getSerializedName() {
            return name;
        }
    }
}
