package dev.spiritstudios.snapper.gui.screen;

import dev.spiritstudios.snapper.Snapper;
import dev.spiritstudios.snapper.SnapperConfig;
import dev.spiritstudios.snapper.gui.widget.ScreenshotListWidget;
import dev.spiritstudios.snapper.util.ScreenshotActions;
import dev.spiritstudios.snapper.util.ScreenshotImage;
import dev.spiritstudios.specter.api.config.RootConfigScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.io.File;

import static dev.spiritstudios.snapper.Snapper.MODID;

public class ScreenshotScreen extends Screen {
    private static final Identifier PANORAMA_BUTTON_ICON = Identifier.of(MODID, "screenshots/panorama");
    private static final Identifier SETTINGS_ICON = Identifier.of(MODID, "screenshots/settings");
    private static final Identifier VIEW_MODE_ICON = Identifier.of(MODID, "screenshots/show_list");
    private final Screen parent;
    ScreenshotListWidget screenshotList;
    private ButtonWidget deleteButton;
    private ButtonWidget renameButton;
    private ButtonWidget viewButton;
    private ButtonWidget copyButton;
    private ButtonWidget openButton;
    private ScreenshotListWidget.@Nullable ScreenshotEntry selectedScreenshot = null;

    public ScreenshotScreen(Screen parent) {
        super(Text.translatable("menu.snapper.screenshotmenu"));
        this.parent = parent;
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

        ButtonWidget folderButton = addDrawableChild(ButtonWidget.builder(
                Text.translatable("button.snapper.folder"),
                button -> Util.getOperatingSystem().open(new File(client.runDirectory, "screenshots"))
        ).width(100).build());

        this.viewButton = addDrawableChild(ButtonWidget.builder(
                Text.translatable("button.snapper.view"),
                button -> {
                    if (selectedScreenshot != null)
                        this.client.setScreen(new ScreenshotViewerScreen(
                                selectedScreenshot.icon,
                                selectedScreenshot.screenshot,
                                selectedScreenshot.screenParent
                        ));
                }
        ).width(100).build());

        ButtonWidget doneButton = addDrawableChild(ButtonWidget.builder(
                ScreenTexts.DONE,
                button -> this.close()
        ).width(100).build());

        this.deleteButton = addDrawableChild(ButtonWidget.builder(
                Text.translatable("button.snapper.delete"),
                button -> {
                    if (selectedScreenshot != null)
                        ScreenshotActions.deleteScreenshot(selectedScreenshot.screenshot, this);
                }
        ).width(74).build());

        this.openButton = addDrawableChild(ButtonWidget.builder(
                Text.translatable("button.snapper.open"),
                button -> {
                    if (selectedScreenshot != null)
                        Util.getOperatingSystem().open(selectedScreenshot.screenshot);
                }
        ).width(74).build());

        this.renameButton = addDrawableChild(ButtonWidget.builder(
                Text.translatable("button.snapper.rename"),
                button -> {
                    if (this.selectedScreenshot != null)
                        client.setScreen(new RenameScreenshotScreen(this.selectedScreenshot.screenshot, this));
                }
        ).width(74).build());

        this.copyButton = addDrawableChild(ButtonWidget.builder(
                Text.translatable("button.snapper.copy"),
                button -> {
                    if (selectedScreenshot != null)
                        Snapper.getPlatformHelper().copyScreenshot(selectedScreenshot.screenshot);
                }
        ).width(74).build());

        DirectionalLayoutWidget verticalButtonLayout = DirectionalLayoutWidget.vertical().spacing(4);

        AxisGridWidget firstRowWidget = verticalButtonLayout.add(new AxisGridWidget(
                308,
                20,
                AxisGridWidget.DisplayAxis.HORIZONTAL
        ));

        firstRowWidget.add(this.deleteButton);
        firstRowWidget.add(this.openButton);
        firstRowWidget.add(this.renameButton);
        firstRowWidget.add(this.copyButton);

        AxisGridWidget secondRowWidget = verticalButtonLayout.add(new AxisGridWidget(
                308,
                20,
                AxisGridWidget.DisplayAxis.HORIZONTAL
        ));

        secondRowWidget.add(folderButton);
        secondRowWidget.add(this.viewButton);
        secondRowWidget.add(doneButton);

        verticalButtonLayout.refreshPositions();
        SimplePositioningWidget.setPos(verticalButtonLayout, 0, this.height - 66, this.width, 64);

        TextIconButtonWidget settingsButton = addDrawableChild(TextIconButtonWidget.builder(
                Text.translatable("config.snapper.snapper.title"),
                button -> this.client.setScreen(
                        new RootConfigScreen(SnapperConfig.HOLDER, this)),
                true
        ).width(20).texture(SETTINGS_ICON, 15, 15).build());

        settingsButton.setPosition(width / 2 - 178, height - 32);


        TextIconButtonWidget viewModeButton = addDrawableChild(TextIconButtonWidget.builder(
                Text.translatable("config.snapper.snapper.view_mode"),
                button -> {
                    screenshotList.toggleGrid();
                    screenshotList.refreshScroll();
                },
                true
        ).width(20).texture(VIEW_MODE_ICON, 15, 15).build());

        viewModeButton.setPosition(width / 2 - 178, height - 56);

        TextIconButtonWidget panoramaButton = addDrawableChild(TextIconButtonWidget.builder(
                Text.translatable("button.snapper.screenshots"),
                button -> this.client.setScreen(new PanoramaViewerScreen(Text.translatable("menu.snapper.panorama").getString(), this)),
                true
        ).width(20).texture(PANORAMA_BUTTON_ICON, 15, 15).build());

        panoramaButton.setPosition(width / 2 + 158, height - 32);
        panoramaButton.setTooltip(Tooltip.of(Text.translatable("button.snapper.panorama.tooltip")));

        this.imageSelected(null);
    }

    public void imageSelected(@Nullable ScreenshotListWidget.ScreenshotEntry screenshot) {
        boolean hasScreenshot = screenshot != null;
        this.copyButton.active = hasScreenshot;
        this.deleteButton.active = hasScreenshot;
        this.openButton.active = hasScreenshot;
        this.renameButton.active = hasScreenshot;
        this.viewButton.active = hasScreenshot;
        this.selectedScreenshot = screenshot;
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
            Snapper.getPlatformHelper().copyScreenshot(selectedScreenshot.screenshot);
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_ENTER && selectedScreenshot != null) {
            if (client == null) return false;
            client.setScreen(new ScreenshotViewerScreen(selectedScreenshot.icon, selectedScreenshot.screenshot, this));
        }

        return false;
    }


    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xffffff);
    }
}
