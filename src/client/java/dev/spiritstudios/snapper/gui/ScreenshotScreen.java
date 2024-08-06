package dev.spiritstudios.snapper.gui;

import dev.spiritstudios.snapper.Snapper;
import dev.spiritstudios.snapper.gui.widget.ScreenshotListWidget;
import dev.spiritstudios.snapper.util.ScreenshotActions;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

import static dev.spiritstudios.snapper.Snapper.MODID;

public class ScreenshotScreen extends Screen {
    private static final Identifier PANORAMA_BUTTON_ICON = Identifier.of(MODID, "screenshots/panorama");
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

        try {
            screenshotList = this.addDrawableChild(new ScreenshotListWidget(client, width, height - 48 - 68, 48, 36, screenshotList, this));
        } catch (IOException e) {
            Snapper.LOGGER.error("Failed to load screenshots", e);
            client.setScreen(parent);
            return;
        }

        ButtonWidget folderButton = addDrawableChild(
                ButtonWidget.builder(Text.translatable("button.snapper.folder"), button ->
                                Util.getOperatingSystem().open(new File(client.runDirectory, "screenshots")))
                        .width(100)
                        .build()
        );

        this.viewButton = addDrawableChild(
                ButtonWidget.builder(Text.translatable("button.snapper.view"), button ->
                                this.client.setScreen(new ScreenshotViewerScreen(selectedScreenshot.iconFileName, selectedScreenshot.icon, selectedScreenshot.iconPath, selectedScreenshot.screenParent)))
                        .width(100)
                        .build()
        );

        ButtonWidget doneButton = addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.close())
                .width(100)
                .build()
        );

        this.deleteButton = addDrawableChild(ButtonWidget.builder(Text.translatable("button.snapper.delete"), button -> ScreenshotActions.deleteScreenshot(selectedScreenshot.screenshot, this))
                .width(74)
                .build()
        );

        this.openButton = addDrawableChild(ButtonWidget.builder(Text.translatable("button.snapper.open"), button -> Util.getOperatingSystem().open(selectedScreenshot.screenshot))
                .width(74)
                .build()
        );

        this.renameButton = addDrawableChild(ButtonWidget.builder(Text.translatable("button.snapper.rename"), button -> ScreenshotActions.renameScreenshot(selectedScreenshot.screenshot, Util.getFormattedCurrentTime() + "_test" + ".png"))
                .width(74)
                .build()
        );

        this.copyButton = addDrawableChild(ButtonWidget.builder(Text.translatable("button.snapper.copy"), button -> ScreenshotActions.copyScreenshot(selectedScreenshot.screenshot))
                .width(74)
                .build()
        );

        DirectionalLayoutWidget verticalButtonLayout = DirectionalLayoutWidget.vertical().spacing(4);
        AxisGridWidget firstRowWidget = verticalButtonLayout.add(new AxisGridWidget(308, 20, AxisGridWidget.DisplayAxis.HORIZONTAL));
        firstRowWidget.add(this.deleteButton);
        firstRowWidget.add(this.openButton);
        firstRowWidget.add(this.renameButton);
        firstRowWidget.add(this.copyButton);
        AxisGridWidget secondRowWidget = verticalButtonLayout.add(new AxisGridWidget(308, 20, AxisGridWidget.DisplayAxis.HORIZONTAL));
        secondRowWidget.add(folderButton);
        secondRowWidget.add(this.viewButton);
        secondRowWidget.add(doneButton);

        verticalButtonLayout.refreshPositions();
        SimplePositioningWidget.setPos(verticalButtonLayout, 0, this.height - 66, this.width, 64);

        TextIconButtonWidget panorama_button = addDrawableChild(
                TextIconButtonWidget.builder(
                        Text.translatable("button.snapper.screenshots"),
                        button -> {
                            this.client.setScreen(new PanoramaViewerScreen(I18n.translate("menu.snapper.panorama"), this));
                        },
                        true
                ).width(20).texture(PANORAMA_BUTTON_ICON, 15, 15).build()
        );
        panorama_button.setPosition(width / 2 + 158, height - 32);
        panorama_button.setTooltip(Tooltip.of(Text.translatable("button.snapper.panorama.tooltip")));

        this.imageSelected(null);
    }

    public void imageSelected(@Nullable ScreenshotListWidget.ScreenshotEntry screenshot) {
        if (screenshot == null) {
            this.copyButton.active = false;
            this.deleteButton.active = false;
            this.openButton.active = false;
            this.renameButton.active = false;
            this.viewButton.active = false;
            this.selectedScreenshot = null;
        } else {
            this.copyButton.active = true;
            this.deleteButton.active = true;
            this.openButton.active = true;
            this.renameButton.active = true;
            this.viewButton.active = true;
            this.selectedScreenshot = screenshot;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xffffff);
    }
}
