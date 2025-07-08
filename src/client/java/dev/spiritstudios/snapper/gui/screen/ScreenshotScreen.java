package dev.spiritstudios.snapper.gui.screen;

import dev.spiritstudios.snapper.Snapper;
import dev.spiritstudios.snapper.SnapperConfig;
import dev.spiritstudios.snapper.gui.widget.ScreenshotListWidget;
import dev.spiritstudios.snapper.util.ScreenshotActions;
import dev.spiritstudios.snapper.util.SnapperUtil;
import dev.spiritstudios.snapper.util.uploading.ScreenshotUploading;
import dev.spiritstudios.specter.api.config.RootConfigScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.util.InputUtil;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Path;

import static dev.spiritstudios.snapper.Snapper.MODID;

public class ScreenshotScreen extends Screen {
    private static final Identifier PANORAMA_BUTTON_ICON = Identifier.of(MODID, "screenshots/panorama");
    private static final Identifier PANORAMA_BUTTON_DISABLED_ICON = Identifier.of(MODID, "screenshots/panorama_disabled");

    private static final Identifier SETTINGS_ICON = Identifier.of(MODID, "screenshots/settings");
    private static Identifier VIEW_MODE_ICON;
    private final Screen parent;
    ScreenshotListWidget screenshotList;
    private ButtonWidget deleteButton;
    private ButtonWidget renameButton;
    private ButtonWidget viewButton;
    private ButtonWidget copyButton;
    private ButtonWidget openButton;
    private ButtonWidget uploadButton;
    private TextIconButtonWidget viewModeButton;
    private ScreenshotListWidget.@Nullable ScreenshotEntry selectedScreenshot = null;
    private boolean showGrid;
    private final boolean isOffline;

    public ScreenshotScreen(Screen parent) {
        super(Text.translatable("menu.snapper.screenshot_menu"));
        this.parent = parent;

        this.showGrid = SnapperConfig.INSTANCE.viewMode.get().equals(ScreenshotViewerScreen.ViewMode.GRID);
        this.isOffline = SnapperUtil.isOfflineAccount();
        VIEW_MODE_ICON = showGrid ? Identifier.of(MODID, "screenshots/show_list") : Identifier.of(MODID, "screenshots/show_grid");
    }

    @Override
    protected void init() {
        if (client == null) return;
        screenshotList = this.addDrawableChild(new ScreenshotListWidget(
                client,
                width,
                height - 48 - 68,
                48,
                36,
                screenshotList,
                this
        ));

        int secondRowButtonWidth = 100;

        ButtonWidget folderButton = addDrawableChild(ButtonWidget.builder(
                Text.translatable("button.snapper.folder"),
                button -> Util.getOperatingSystem().open(SnapperUtil.getConfiguredScreenshotDirectory())
        ).width(secondRowButtonWidth).build());


        this.openButton = addDrawableChild(ButtonWidget.builder(
                Text.translatable("button.snapper.open"),
                button -> {
                    if (selectedScreenshot != null)
                        Util.getOperatingSystem().open(selectedScreenshot.path);
                }
        ).width(secondRowButtonWidth).build());

        ButtonWidget doneButton = addDrawableChild(ButtonWidget.builder(
                ScreenTexts.DONE,
                button -> this.close()
        ).width(secondRowButtonWidth).build());

        int firstRowButtonWidth = 58;

        this.deleteButton = addDrawableChild(ButtonWidget.builder(
                Text.translatable("button.snapper.delete"),
                button -> {
                    if (selectedScreenshot != null)
                        ScreenshotActions.deleteScreenshot(selectedScreenshot.path, this);
                }
        ).width(firstRowButtonWidth).build());

        this.renameButton = addDrawableChild(ButtonWidget.builder(
                Text.translatable("button.snapper.rename"),
                button -> {
                    if (this.selectedScreenshot != null)
                        client.setScreen(new RenameScreenshotScreen(this.selectedScreenshot.path, this));
                }
        ).width(firstRowButtonWidth).build());

        this.copyButton = addDrawableChild(ButtonWidget.builder(
                Text.translatable("button.snapper.copy"),
                button -> {
                    if (selectedScreenshot != null)
                        Snapper.getPlatformHelper().copyScreenshot(selectedScreenshot.path);
                }
        ).width(firstRowButtonWidth).build());

        this.viewButton = addDrawableChild(ButtonWidget.builder(
                Text.translatable("button.snapper.view"),
                button -> {
                    if (selectedScreenshot != null)
                        this.client.setScreen(new ScreenshotViewerScreen(
                                selectedScreenshot.icon,
                                selectedScreenshot.path,
                                selectedScreenshot.screenParent
                        ));
                }
        ).width(firstRowButtonWidth).build());

        this.uploadButton = addDrawableChild(ButtonWidget.builder(Text.translatable("button.snapper.upload"), button -> {
            if (selectedScreenshot == null) return;

            button.active = false;
            ScreenshotUploading.upload(selectedScreenshot.path)
                    .thenRun(() -> button.active = true);
        }).width(firstRowButtonWidth).build());

        if (isOffline) {
            this.uploadButton.setTooltip(Tooltip.of(Text.translatable("button.snapper.upload.tooltip")));
        }

        DirectionalLayoutWidget verticalButtonLayout = DirectionalLayoutWidget.vertical().spacing(4);

        AxisGridWidget firstRowWidget = verticalButtonLayout.add(new AxisGridWidget(
                308,
                20,
                AxisGridWidget.DisplayAxis.HORIZONTAL
        ));

        firstRowWidget.add(this.deleteButton);
        firstRowWidget.add(this.renameButton);
        firstRowWidget.add(this.copyButton);
        firstRowWidget.add(this.viewButton);
        firstRowWidget.add(this.uploadButton);

        AxisGridWidget secondRowWidget = verticalButtonLayout.add(new AxisGridWidget(
                308,
                20,
                AxisGridWidget.DisplayAxis.HORIZONTAL
        ));

        secondRowWidget.add(folderButton);
        secondRowWidget.add(this.openButton);
        secondRowWidget.add(doneButton);

        verticalButtonLayout.refreshPositions();
        SimplePositioningWidget.setPos(verticalButtonLayout, 0, this.height - 66, this.width, 64);

        TextIconButtonWidget settingsButton = addDrawableChild(TextIconButtonWidget.builder(
                Text.translatable("config.snapper.snapper.title"),
                button -> this.client.setScreen(
                        new RootConfigScreen(SnapperConfig.HOLDER, new ScreenshotScreen(this.parent))),
                true
        ).width(20).texture(SETTINGS_ICON, 15, 15).build());

        settingsButton.setPosition(width / 2 - 178, height - 32);


        this.viewModeButton = addDrawableChild(TextIconButtonWidget.builder(
                Text.translatable("config.snapper.snapper.viewMode"),
                button -> this.toggleGrid(),
                true
        ).width(20).texture(VIEW_MODE_ICON, 15, 15).build());

        viewModeButton.setPosition(width / 2 - 178, height - 56);

        Path panoramaDir = SnapperUtil.getConfiguredScreenshotDirectory().resolve("panorama");
        boolean hasPanorama = SnapperUtil.panoramaPresent(panoramaDir);

        TextIconButtonWidget panoramaButton = addDrawableChild(TextIconButtonWidget.builder(
                Text.translatable("button.snapper.screenshots"),
                button -> this.client.setScreen(new PanoramaViewerScreen(Text.translatable("menu.snapper.panorama").getString(), this)),
                true
        ).width(20).texture(hasPanorama ? PANORAMA_BUTTON_ICON : PANORAMA_BUTTON_DISABLED_ICON, 15, 15).build());

        panoramaButton.active = hasPanorama;
        panoramaButton.setPosition(width / 2 + 158, height - 32);

        panoramaButton.setTooltip(Tooltip.of(Text.translatable(hasPanorama ?
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
        screenshotList.toggleGrid();
        screenshotList.refreshScroll();
        this.showGrid = !this.showGrid;
        VIEW_MODE_ICON = showGrid ? Identifier.of(MODID, "screenshots/show_list") : Identifier.of(MODID, "screenshots/show_grid");

        remove(this.viewModeButton);
        this.viewModeButton = addDrawableChild(TextIconButtonWidget.builder(
                Text.translatable("config.snapper.snapper.viewMode"),
                button -> this.toggleGrid(),
                true
        ).width(20).texture(VIEW_MODE_ICON, 15, 15).build());
        viewModeButton.setPosition(width / 2 - 178, height - 56);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) return true;

        long handle = MinecraftClient.getInstance().getWindow().getHandle();
        if (keyCode == GLFW.GLFW_KEY_F5) {
            if (client == null) return false;

            client.setScreen(new ScreenshotScreen(this.parent));
            return true;
        }

        if (
                (InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_LEFT_CONTROL) || InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_RIGHT_CONTROL)) &&
                        InputUtil.isKeyPressed(handle, InputUtil.GLFW_KEY_C) &&
                        selectedScreenshot != null
        ) {
            Snapper.getPlatformHelper().copyScreenshot(selectedScreenshot.path);
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_ENTER && selectedScreenshot != null) {
            if (client == null) return false;
            client.setScreen(new ScreenshotViewerScreen(selectedScreenshot.icon, selectedScreenshot.path, this));
        }

        return false;
    }

    @Override
    public void close() {
        SnapperConfig.HOLDER.save();
        super.close();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xffffff);
    }
}
