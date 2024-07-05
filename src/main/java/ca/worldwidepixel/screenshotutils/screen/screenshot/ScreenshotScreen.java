package ca.worldwidepixel.screenshotutils.screen.screenshot;

import ca.worldwidepixel.screenshotutils.ScreenshotUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

public class ScreenshotScreen extends Screen {
    public ScreenshotScreen() {
        super(Text.translatable("menu.screenshotutils.screenshotmenu"));
    }

    public File[] getScreenshots() {
        File screenshotDir = new File(client.runDirectory, "screenshots");
        File[] screenshots = screenshotDir.listFiles();
        Arrays.sort(screenshots);
        return screenshots;
    }

    public ButtonWidget testButton;

    @Override
    protected void init() {
        ScreenshotListWidget screenshotList = null;
        try {
            screenshotList = new ScreenshotListWidget(client, width, height - 48 - 48, 48, 36);
        } catch (FileNotFoundException e) {
            ScreenshotUtils.LOGGER.error("SCREENSHOT MENU FAILED; FILE RENAMED/DELETED", e);
            client.setScreen(new TitleScreen());
        } catch (IOException e) {
            ScreenshotUtils.LOGGER.error("SCREENSHOT LOADING FAILED; FILE READER EXCEPTION", e);
            client.setScreen(new TitleScreen());
        }

        this.addDrawableChild(screenshotList);

        /*for (int i = 0; i < getScreenshots().length; i++) {
            this.addDrawableChild(new TextWidget(Text.literal(getScreenshots()[i].getName()), this.textRenderer))
                    .setPosition(width / 2 - textRenderer.getWidth(getScreenshots()[i].getName()) / 2, 40 + i * 16);
        } */

        /*testButton = ButtonWidget.builder(Text.literal("Debug Button"), button -> {
            for (File screenshot : getScreenshots()) {
                ScreenshotUtils.LOGGER.info(screenshot.getName());
            }
        })
                .dimensions(10, 10, 150, 20)
                .tooltip(Tooltip.of(Text.literal("List screenshots in Game directory")))
                .build();

        addDrawableChild(testButton);*/

        addDrawableChild(ButtonWidget.builder(Text.translatable("button.screenshotutils.folder"), button -> {
                    Util.getOperatingSystem().open(new File(client.runDirectory, "screenshots"));
                })
                .dimensions(width / 2 - 150 - 2, height - 32, 150, 20)
                .build()
        );

        addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.close())
                .dimensions(width / 2 + 2, height - 32, 150, 20)
                .build()
        );
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 16777215);
    }
}
