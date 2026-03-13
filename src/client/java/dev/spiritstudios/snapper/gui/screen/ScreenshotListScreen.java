package dev.spiritstudios.snapper.gui.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.serialization.Codec;
import dev.spiritstudios.snapper.Snapper;
import dev.spiritstudios.snapper.SnapperConfig;
import dev.spiritstudios.snapper.gui.toast.SnapperToast;
import dev.spiritstudios.snapper.gui.widget.ScreenshotListWidget;
import dev.spiritstudios.snapper.gui.widget.ScreenshotsWidget;
import dev.spiritstudios.snapper.util.PlatformHelper;
import dev.spiritstudios.snapper.util.ScreenshotActions;
import dev.spiritstudios.snapper.util.SnapperUtil;
import dev.spiritstudios.snapper.util.uploading.ScreenshotUploading;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.EqualSpacingLayout;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CommonColors;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.nio.file.Path;

public class ScreenshotListScreen extends Screen {
    private static final ResourceLocation PANORAMA_BUTTON_ICON = Snapper.id("screenshots/panorama");
    private static final ResourceLocation PANORAMA_BUTTON_DISABLED_ICON = Snapper.id("screenshots/panorama_disabled");

    private static final ResourceLocation SETTINGS_ICON = Snapper.id("screenshots/settings");

    private static final ResourceLocation VIEW_MODE_ICON_LIST = Snapper.id("screenshots/show_list");

    private static final ResourceLocation VIEW_MODE_ICON_GRID = Snapper.id("screenshots/show_grid");

    private final Screen parent;
    private final boolean isOffline;

    private ScreenshotsWidget screenshots = null;

    private Button deleteButton;
    private Button renameButton;
    private Button viewButton;
    private Button copyButton;
    private Button openButton;
    private Button uploadButton;
    private SpriteIconButton viewModeButton;
    private @Nullable ScreenshotListWidget.ScreenshotEntry selectedScreenshot = null;

    public ScreenshotListScreen(Screen parent) {
        super(Component.translatable("menu.snapper.screenshot_menu"));
        this.parent = parent;
        this.isOffline = SnapperUtil.isOfflineAccount();
    }

    public synchronized void refresh() {
        recreateList();
        recreateViewModeButton();
    }

    private void recreateList() {
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
    }

    private void recreateViewModeButton() {
        if (viewModeButton != null) {
            removeWidget(this.viewModeButton);
        }

        this.viewModeButton = addRenderableWidget(SpriteIconButton.builder(
                Component.translatable("config.snapper.viewMode"),
                button -> this.toggleGrid(),
                true
        ).width(20).sprite(SnapperConfig.HOLDER.get().viewMode() == ViewMode.LIST ? VIEW_MODE_ICON_LIST : VIEW_MODE_ICON_GRID, 15, 15).build());
        viewModeButton.setPosition(width / 2 - 178, height - 56);
    }

    @Override
    protected void init() {
        assert minecraft != null;

        recreateList();

        int secondRowButtonWidth = 100;

        Button folderButton = addRenderableWidget(Button.builder(
                Component.translatable("button.snapper.folder"),
                button -> Util.getPlatform().openPath(SnapperUtil.getConfiguredScreenshotDirectory())
        ).width(secondRowButtonWidth).build());


        this.openButton = addRenderableWidget(Button.builder(
                Component.translatable("button.snapper.open"),
                button -> {
                    if (selectedScreenshot != null) {
                        Util.getPlatform().openPath(selectedScreenshot.icon.getPath());
                    }
                }
        ).width(secondRowButtonWidth).build());

        Button doneButton = addRenderableWidget(Button.builder(
                CommonComponents.GUI_DONE,
                button -> this.onClose()
        ).width(secondRowButtonWidth).build());

        int firstRowButtonWidth = 58;

        this.deleteButton = addRenderableWidget(Button.builder(
                Component.translatable("button.snapper.delete"),
                button -> {
                    if (selectedScreenshot != null) {
                        ScreenshotActions.deleteScreenshot(selectedScreenshot.icon.getPath(), this);
                    }
                }
        ).width(firstRowButtonWidth).build());

        this.renameButton = addRenderableWidget(Button.builder(
                Component.translatable("button.snapper.rename"),
                button -> {
                    if (this.selectedScreenshot != null) {
                        minecraft.setScreen(new ScreenshotRenameScreen(this.selectedScreenshot.icon.getPath(), this));
                    }
                }
        ).width(firstRowButtonWidth).build());

        this.copyButton = addRenderableWidget(Button.builder(
                Component.translatable("button.snapper.copy"),
                button -> {
                    if (selectedScreenshot != null) {
                        PlatformHelper.INSTANCE.copyScreenshot(selectedScreenshot.icon.getPath());
                    }
                }
        ).width(firstRowButtonWidth).build());

        this.viewButton = addRenderableWidget(Button.builder(
                Component.translatable("button.snapper.view"),
                button -> {
                    if (selectedScreenshot != null) {
                        this.minecraft.setScreen(new ScreenshotViewerScreen(
                                selectedScreenshot.icon,
                                selectedScreenshot.icon.getPath(),
                                selectedScreenshot.screenParent
                        ));
                    }
                }
        ).width(firstRowButtonWidth).build());

        this.uploadButton = addRenderableWidget(Button.builder(Component.translatable("button.snapper.upload"), button -> {
            if (selectedScreenshot == null) return;

            button.active = false;
            ScreenshotUploading.upload(selectedScreenshot.icon.getPath())
                    .thenRun(() -> button.active = true);
        }).width(firstRowButtonWidth).build());

        if (isOffline) {
            this.uploadButton.setTooltip(Tooltip.create(Component.translatable("button.snapper.upload.tooltip")));
        }

        LinearLayout verticalButtonLayout = LinearLayout.vertical()
                .spacing(4);

        EqualSpacingLayout firstRowWidget = verticalButtonLayout.addChild(new EqualSpacingLayout(
                308,
                20,
                EqualSpacingLayout.Orientation.HORIZONTAL
        ));

        firstRowWidget.addChild(this.deleteButton);
        firstRowWidget.addChild(this.renameButton);
        firstRowWidget.addChild(this.copyButton);
        firstRowWidget.addChild(this.viewButton);
        firstRowWidget.addChild(this.uploadButton);

        EqualSpacingLayout secondRowWidget = verticalButtonLayout.addChild(new EqualSpacingLayout(
                308,
                20,
                EqualSpacingLayout.Orientation.HORIZONTAL
        ));

        secondRowWidget.addChild(folderButton);
        secondRowWidget.addChild(openButton);
        secondRowWidget.addChild(doneButton);

        verticalButtonLayout.arrangeElements();
        FrameLayout.centerInRectangle(verticalButtonLayout, 0, this.height - 66, this.width, 64);

        SpriteIconButton settingsButton = addRenderableWidget(SpriteIconButton.builder(
                Component.translatable("config.snapper.title"),
                button -> this.minecraft.setScreen(
                        new ConfigScreen(new ScreenshotListScreen(this.parent))),
                true
        ).width(20).sprite(SETTINGS_ICON, 15, 15).build());

        settingsButton.setPosition(width / 2 - 178, height - 32);

        recreateViewModeButton();

        Path panoramaDir = SnapperUtil.getConfiguredScreenshotDirectory().resolve("panorama");
        boolean hasPanorama = SnapperUtil.panoramaPresent(panoramaDir);

        SpriteIconButton panoramaButton = addRenderableWidget(SpriteIconButton.builder(
                Component.translatable("button.snapper.screenshots"),
                button -> this.minecraft.setScreen(new PanoramaViewerScreen(Component.translatable("menu.snapper.panorama").getString(), this)),
                true
        ).width(20).sprite(hasPanorama ? PANORAMA_BUTTON_ICON : PANORAMA_BUTTON_DISABLED_ICON, 15, 15).build());

        panoramaButton.active = hasPanorama;
        panoramaButton.setPosition(width / 2 + 158, height - 32);

        panoramaButton.setTooltip(Tooltip.create(Component.translatable(hasPanorama ?
                "button.snapper.panorama.tooltip" :
                "text.snapper.panorama_encourage")));


        this.imageSelected(selectedScreenshot);
    }

    public void imageSelected(@Nullable ScreenshotListWidget.ScreenshotEntry screenshot) {
        boolean hasScreenshot = screenshot != null;
        this.copyButton.active = hasScreenshot;
        this.deleteButton.active = hasScreenshot;
        this.openButton.active = hasScreenshot;
        this.renameButton.active = hasScreenshot;
        this.viewButton.active = hasScreenshot;
        this.selectedScreenshot = screenshot;
        this.uploadButton.active = !isOffline && hasScreenshot;
    }

    public void toggleGrid() {
        SnapperConfig.edit(m -> m.viewMode = SnapperConfig.HOLDER.get().viewMode() == ViewMode.GRID ? ViewMode.LIST : ViewMode.GRID);

        refresh();
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        assert minecraft != null;

        if (super.keyPressed(input)) {
            return true;
        }

        if (input.key() == InputConstants.KEY_F5) {
            minecraft.setScreen(new ScreenshotListScreen(this.parent));
            return true;
        }

        if (selectedScreenshot == null) return false;

        if ((input.modifiers() & InputConstants.MOD_CONTROL) != 0 && input.key() == InputConstants.KEY_C) {
            PlatformHelper.INSTANCE.copyScreenshot(selectedScreenshot.icon.getPath());
            SnapperToast.push(SnapperToast.Type.SCREENSHOT, Component.translatable("toast.snapper.screenshot.copy"), null);
            return true;
        }

        if (input.key() == InputConstants.KEY_RETURN) {
            minecraft.setScreen(new ScreenshotViewerScreen(selectedScreenshot.icon, selectedScreenshot.icon.getPath(), this));
            return true;
        }

        return false;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 20, CommonColors.WHITE);
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
