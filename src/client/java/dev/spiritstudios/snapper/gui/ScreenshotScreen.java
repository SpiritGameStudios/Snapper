package dev.spiritstudios.snapper.gui;

import dev.spiritstudios.snapper.Snapper;
import dev.spiritstudios.snapper.gui.widget.ScreenshotListWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.io.File;
import java.io.IOException;

import static dev.spiritstudios.snapper.Snapper.MODID;

public class ScreenshotScreen extends Screen {
    private Screen parent;
    private static final Identifier PANORAMA_BUTTON_ICON = Identifier.of(MODID, "screenshots/panorama");
    ScreenshotListWidget screenshotList;

    public ScreenshotScreen(Screen parent) {
        super(Text.translatable("menu.snapper.screenshotmenu"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        if (client == null) return;

        try {
            screenshotList = this.addDrawableChild(new ScreenshotListWidget(client, width, height - 48 - 48, 48, 36, screenshotList, this));
        } catch (IOException e) {
            Snapper.LOGGER.error("Failed to load screenshots", e);
            client.setScreen(parent);
            return;
        }

        addDrawableChild(
                ButtonWidget.builder(Text.translatable("button.snapper.folder"), button ->
                                Util.getOperatingSystem().open(new File(client.runDirectory, "screenshots")))
                        .dimensions(width / 2 - 152, height - 32, 150, 20)
                        .build()
        );

        addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.close())
                .dimensions(width / 2 + 2, height - 32, 150, 20)
                .build()
        );
        TextIconButtonWidget panorama_button = addDrawableChild(
                TextIconButtonWidget.builder(
                            Text.translatable("button.snapper.screenshots"),
                            button -> {
                                this.client.setScreen(new PanoramaViewerScreen(I18n.translate("menu.snapper.panorama"), this));
                            },
                            true
                    ).width(20).texture(PANORAMA_BUTTON_ICON, 15, 15).build()
            );
        panorama_button.setPosition(width / 2 + 156, height - 32);
        panorama_button.setTooltip(Tooltip.of(Text.translatable("button.snapper.panorama.tooltip")));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xffffff);
    }
}
