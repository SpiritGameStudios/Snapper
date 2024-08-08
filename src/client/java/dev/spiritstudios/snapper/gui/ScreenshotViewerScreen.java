package dev.spiritstudios.snapper.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.spiritstudios.snapper.Snapper;
import dev.spiritstudios.snapper.util.ScreenshotActions;
import dev.spiritstudios.snapper.util.ScreenshotIcon;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AxisGridWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class ScreenshotViewerScreen extends Screen {
    private final MinecraftClient client = MinecraftClient.getInstance();
    private final ScreenshotIcon icon;
    private Path iconPath;
    private final String title;
    private final int imageWidth;
    private final int imageHeight;
    private final Screen parent;
    private final File screenshot;

    private static final Identifier MENU_DECOR_BACKGROUND_TEXTURE = Identifier.ofVanilla("textures/gui/menu_list_background.png");
    private static final Identifier INWORLD_MENU_DECOR_BACKGROUND_TEXTURE = Identifier.ofVanilla("textures/gui/inworld_menu_list_background.png");

    public ScreenshotViewerScreen(ScreenshotIcon icon, File screenshot, Screen parent) {
        super(Text.translatable("menu.snapper.viewermenu"));
        this.parent = parent;

        try {
            this.iconPath = Path.of(screenshot.getCanonicalPath());
        } catch (IOException e) {
            this.iconPath = null;
            Snapper.LOGGER.error("FAILED TO GET PATH OF IMAGE");
            client.setScreen(this.parent);
        }

        BufferedImage img = null;
        try {
            img = ImageIO.read(new File(String.valueOf(this.iconPath)));
        } catch (IOException e) {
            Snapper.LOGGER.error("Image failed to read.");
            this.client.setScreen(parent);
        }

        this.icon = icon;
        this.title = screenshot.getName();
        this.imageWidth = img != null ? img.getWidth() : 0;
        this.imageHeight = img != null ? img.getHeight() : 0;
        this.screenshot = screenshot;
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    @Override
    protected void init() {

        // OPEN FOLDER

        ButtonWidget folderButton = addDrawableChild(
                ButtonWidget.builder(Text.translatable("button.snapper.folder"), button ->
                        Util.getOperatingSystem().open(new File(client.runDirectory, "screenshots")))
                .width(100)
                .build()
        );

        // OPEN IMAGE EXTERNALLY

        ButtonWidget openButton = addDrawableChild(ButtonWidget.builder(Text.translatable("button.snapper.open"), button ->
                        Util.getOperatingSystem().open(this.iconPath))
                .width(100)
                .build()
        );

        // EXIT PAGE

        ButtonWidget doneButton = addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button ->
                        this.close())
                .width(100)
                .build()
        );

        // DELETE SCREENSHOT

        ButtonWidget deleteButton = addDrawableChild(ButtonWidget.builder(Text.translatable("button.snapper.delete"), button -> ScreenshotActions.deleteScreenshot(this.screenshot, this.parent))
                .width(100)
                .build()
        );

        // RENAME SCREENSHOT

        ButtonWidget renameButton = addDrawableChild(ButtonWidget.builder(Text.translatable("button.snapper.rename"), button -> {
            if (this.screenshot != null) {
                client.setScreen(new RenameScreenshotScreen(this.screenshot, this.parent));
            }
        })
                .width(100)
                .build());

        // COPY SCREENSHOT

        ButtonWidget copyButton = addDrawableChild(ButtonWidget.builder(Text.translatable("button.snapper.copy"), button -> ScreenshotActions.copyScreenshot(this.screenshot))
                .width(100)
                .build()
        );

        DirectionalLayoutWidget verticalButtonLayout = DirectionalLayoutWidget.vertical().spacing(4);
        AxisGridWidget firstRowWidget = verticalButtonLayout.add(new AxisGridWidget(308, 20, AxisGridWidget.DisplayAxis.HORIZONTAL));
        firstRowWidget.add(deleteButton);
        firstRowWidget.add(renameButton);
        firstRowWidget.add(copyButton);
        AxisGridWidget secondRowWidget = verticalButtonLayout.add(new AxisGridWidget(308, 20, AxisGridWidget.DisplayAxis.HORIZONTAL));
        secondRowWidget.add(openButton);
        secondRowWidget.add(folderButton);
        secondRowWidget.add(doneButton);
        verticalButtonLayout.refreshPositions();
        SimplePositioningWidget.setPos(verticalButtonLayout, 0, this.height - 66, this.width, 64);

    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.drawMenuBackground(context);
        this.drawHeaderAndFooterSeparators(context);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 16777215);
        int finalHeight = this.height - 48 - 68;
        float scaleFactor = (float) finalHeight / imageHeight;
        int finalWidth = (int) (imageWidth * scaleFactor);

        context.drawTexture(this.icon.getTextureId(), (this.width / 2) - (finalWidth / 2), this.height - 68 - finalHeight, 0, 0, finalWidth, finalHeight, finalWidth, finalHeight);
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) renderDebugInfo(context);
    }

    private void renderDebugInfo(DrawContext context) {
        context.getMatrices().push();
        int finalHeight = this.height - 48 - 48;
        float scaleFactor = (float) finalHeight / imageHeight;
        int finalWidth = (int) (imageWidth * scaleFactor);

        context.drawCenteredTextWithShadow(this.textRenderer, "Image Size: %dx%d".formatted(imageWidth, imageHeight), this.width / 2, 30, 0xffffff);
        context.drawCenteredTextWithShadow(this.textRenderer, "Screen Size: %dx%d".formatted(this.width, this.height), this.width / 2, 40, 0xffffff);
        context.drawCenteredTextWithShadow(this.textRenderer, "Scale Factor: %s".formatted(scaleFactor), this.width / 2, 50, 0xffffff);
        context.drawCenteredTextWithShadow(this.textRenderer, "Scaled Size: %dx%d".formatted(finalWidth, finalHeight), this.width / 2, 60, 0xffffff);
    }

    private void drawMenuBackground(DrawContext context) {
        RenderSystem.enableBlend();
        Identifier identifier = this.client.world == null ? MENU_DECOR_BACKGROUND_TEXTURE : INWORLD_MENU_DECOR_BACKGROUND_TEXTURE;
        context.drawTexture(
                identifier,
                0,
                48,
                0,
                0,
                width,
                height - 68 - 48,
                32,
                32
        );
        RenderSystem.disableBlend();
    }

    private void drawHeaderAndFooterSeparators(DrawContext context) {
        RenderSystem.enableBlend();
        Identifier identifier = this.client.world == null ? Screen.HEADER_SEPARATOR_TEXTURE : Screen.INWORLD_HEADER_SEPARATOR_TEXTURE;
        Identifier identifier2 = this.client.world == null ? Screen.FOOTER_SEPARATOR_TEXTURE : Screen.INWORLD_FOOTER_SEPARATOR_TEXTURE;
        context.drawTexture(identifier, 0, 48 - 2, 0, 0, width, 2, 32, 2);
        context.drawTexture(identifier2, 0, height - 68, 0, 0, width, 2, 32, 2);
        RenderSystem.disableBlend();
    }
}
