package dev.spiritstudios.snapper.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.spiritstudios.snapper.Snapper;
import dev.spiritstudios.snapper.util.ScreenshotActions;
import dev.spiritstudios.snapper.util.ScreenshotImage;
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
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class ScreenshotViewerScreen extends Screen {
    private static final Identifier MENU_DECOR_BACKGROUND_TEXTURE = Identifier.ofVanilla("textures/gui/menu_list_background.png");
    private static final Identifier INWORLD_MENU_DECOR_BACKGROUND_TEXTURE = Identifier.ofVanilla("textures/gui/inworld_menu_list_background.png");
    private final MinecraftClient client = MinecraftClient.getInstance();
    private final ScreenshotImage icon;
    private final String title;
    private final int imageWidth;
    private final int imageHeight;
    private final Screen parent;
    private final File screenshot;
    private final @Nullable List<File> screenshots;
    private final int screenshotIndex;
    private Path iconPath;

    public ScreenshotViewerScreen(ScreenshotImage icon, File screenshot, Screen parent) {
        this(icon, screenshot, parent, null);
    }

    public ScreenshotViewerScreen(ScreenshotImage icon, File screenshot, Screen parent, @Nullable List<File> screenshots) {
        super(Text.translatable("menu.snapper.viewermenu"));
        this.parent = parent;

        try {
            this.iconPath = Path.of(screenshot.getCanonicalPath());
        } catch (IOException e) {
            this.iconPath = null;
            Snapper.LOGGER.error("FAILED TO GET PATH OF IMAGE");
            client.setScreen(this.parent);
        }

        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(String.valueOf(this.iconPath)));
        } catch (IOException e) {
            Snapper.LOGGER.error("Image failed to read.");
            this.client.setScreen(parent);
        }

        this.icon = icon;
        this.title = screenshot.getName();

        this.imageWidth = image != null ? image.getWidth() : 0;
        this.imageHeight = image != null ? image.getHeight() : 0;

        this.screenshot = screenshot;
        this.screenshots = screenshots;

        if (this.screenshots != null) {
            this.screenshotIndex = this.screenshots.indexOf(this.screenshot);
        } else {
            this.screenshotIndex = -1;
        }
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    @Override
    protected void init() {

        // OPEN IMAGE EXTERNALLY

        ButtonWidget openButton = addDrawableChild(ButtonWidget.builder(
                Text.translatable("button.snapper.open"),
                button -> Util.getOperatingSystem().open(this.iconPath)
        ).width(100).build());

        // OPEN FOLDER

        ButtonWidget folderButton = addDrawableChild(ButtonWidget.builder(
                                Text.translatable("button.snapper.folder"),
                                button -> Util.getOperatingSystem().open(new File(client.runDirectory, "screenshots"))
                        )
                        .width(100)
                        .build()
        );

        // EXIT PAGE

        ButtonWidget doneButton = addDrawableChild(ButtonWidget.builder(
                ScreenTexts.DONE,
                button -> this.close()
        ).width(100).build());

        // DELETE SCREENSHOT

        ButtonWidget deleteButton = addDrawableChild(ButtonWidget.builder(
                Text.translatable("button.snapper.delete"),
                button -> ScreenshotActions.deleteScreenshot(this.screenshot, this.parent)
        ).width(100).build());

        // RENAME SCREENSHOT

        ButtonWidget renameButton = addDrawableChild(ButtonWidget.builder(
                Text.translatable("button.snapper.rename"),
                button -> {
                    if (this.screenshot != null)
                        client.setScreen(new RenameScreenshotScreen(this.screenshot, this.parent));
                }
        ).width(100).build());

        // COPY SCREENSHOT

        ButtonWidget copyButton = addDrawableChild(ButtonWidget.builder(
                Text.translatable("button.snapper.copy"),
                button -> Snapper.getPlatformHelper().copyScreenshot(this.screenshot)
        ).width(100).build());

        DirectionalLayoutWidget verticalButtonLayout = DirectionalLayoutWidget.vertical().spacing(4);

        AxisGridWidget firstRowWidget = verticalButtonLayout.add(new AxisGridWidget(
                308,
                20,
                AxisGridWidget.DisplayAxis.HORIZONTAL)
        );

        firstRowWidget.add(deleteButton);
        firstRowWidget.add(renameButton);
        firstRowWidget.add(copyButton);

        AxisGridWidget secondRowWidget = verticalButtonLayout.add(new AxisGridWidget(
                308,
                20,
                AxisGridWidget.DisplayAxis.HORIZONTAL)
        );

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
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);

        int finalHeight = this.height - 48 - 68;
        float scaleFactor = (float) finalHeight / imageHeight;
        int finalWidth = (int) (imageWidth * scaleFactor);

        context.drawTexture(
                this.icon.getTextureId(),
                (this.width / 2) - (finalWidth / 2),
                this.height - 68 - finalHeight,
                0,
                0,
                finalWidth,
                finalHeight,
                finalWidth,
                finalHeight
        );

        if (screenshotIndex != -1 && screenshots != null) {
            context.drawCenteredTextWithShadow(
                    this.textRenderer,
                    "Screenshot %d/%d".formatted(screenshotIndex + 1, screenshots.size()),
                    this.width / 2,
                    30,
                    0xFFFFFF
            );
        }

        if (FabricLoader.getInstance().isDevelopmentEnvironment()) renderDebugInfo(context);
    }

    private void renderDebugInfo(DrawContext context) {
        context.getMatrices().push();
        int finalHeight = this.height - 48 - 48;
        float scaleFactor = (float) finalHeight / imageHeight;
        int finalWidth = (int) (imageWidth * scaleFactor);

        context.drawCenteredTextWithShadow(
                this.textRenderer,
                "Image Size: %dx%d".formatted(imageWidth, imageHeight),
                this.width / 2,
                40,
                0xFFFFFF
        );

        context.drawCenteredTextWithShadow(
                this.textRenderer,
                "Screen Size: %dx%d".formatted(this.width, this.height),
                this.width / 2,
                50,
                0xFFFFFF
        );

        context.drawCenteredTextWithShadow(this.textRenderer,
                "Scale Factor: %s".formatted(scaleFactor),
                this.width / 2,
                60,
                0xFFFFFF
        );

        context.drawCenteredTextWithShadow(
                this.textRenderer,
                "Scaled Size: %dx%d".formatted(finalWidth, finalHeight),
                this.width / 2,
                70,
                0xFFFFFF
        );
    }

    private void drawMenuBackground(DrawContext context) {
        RenderSystem.enableBlend();
        Identifier texture = this.client.world == null ? MENU_DECOR_BACKGROUND_TEXTURE : INWORLD_MENU_DECOR_BACKGROUND_TEXTURE;
        context.drawTexture(
                texture,
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
        Identifier headerTexture = this.client.world == null ? Screen.HEADER_SEPARATOR_TEXTURE : Screen.INWORLD_HEADER_SEPARATOR_TEXTURE;
        Identifier footerTexture = this.client.world == null ? Screen.FOOTER_SEPARATOR_TEXTURE : Screen.INWORLD_FOOTER_SEPARATOR_TEXTURE;
        context.drawTexture(
                headerTexture,
                0,
                48 - 2,
                0,
                0,
                width,
                2,
                32,
                2
        );

        context.drawTexture(
                footerTexture,
                0,
                height - 68,
                0,
                0,
                width,
                2,
                32,
                2
        );

        RenderSystem.disableBlend();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        Snapper.LOGGER.debug(String.format("SCROLL DEBUG 1 %s %s", this.screenshotIndex, this.screenshots == null));
        if (this.screenshotIndex != -1 && this.screenshots != null) {
            Snapper.LOGGER.debug(String.format("SCROLL DEBUG 2 %s %s", this.screenshots.size(), this.screenshotIndex));
            if (keyCode == GLFW.GLFW_KEY_LEFT) {
                File previousImageFile = screenshots.getLast();;
                if (this.screenshotIndex >= 1) {
                    previousImageFile = screenshots.get(screenshotIndex - 1);
                }
                ScreenshotImage previousImage = ScreenshotImage.of(previousImageFile, client.getTextureManager());
                Snapper.LOGGER.debug(String.format("SCROLL DEBUG 3a %s", previousImageFile.getName()));
                client.setScreen(new ScreenshotViewerScreen(previousImage, previousImageFile, this.parent, this.screenshots));
            }
            if (keyCode == GLFW.GLFW_KEY_RIGHT) {
                File nextImageFile = screenshots.getFirst();
                if (this.screenshotIndex < this.screenshots.size() - 1) {
                    nextImageFile = screenshots.get(screenshotIndex + 1);
                }
                ScreenshotImage nextImage = ScreenshotImage.of(nextImageFile, client.getTextureManager());
                Snapper.LOGGER.debug(String.format("SCROLL DEBUG 3b %s", nextImageFile.getName()));
                client.setScreen(new ScreenshotViewerScreen(nextImage, nextImageFile, this.parent, this.screenshots));
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
