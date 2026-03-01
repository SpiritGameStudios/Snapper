package dev.spiritstudios.snapper.gui.screen;

import dev.spiritstudios.snapper.Snapper;
import dev.spiritstudios.snapper.util.ScreenshotTexture;
import dev.spiritstudios.snapper.util.ScreenshotActions;
import dev.spiritstudios.snapper.util.SnapperUtil;
import dev.spiritstudios.snapper.util.uploading.ScreenshotUploading;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.EqualSpacingLayout;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CommonColors;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ScreenshotViewerScreen extends Screen {
    private static final ResourceLocation MENU_DECOR_BACKGROUND_TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/menu_list_background.png");
    private static final ResourceLocation INWORLD_MENU_DECOR_BACKGROUND_TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/inworld_menu_list_background.png");

    private final Minecraft client = Minecraft.getInstance();
    private final ScreenshotTexture image;
    private final String title;
    private final int imageWidth;
    private final int imageHeight;
    private final Screen parent;
    private final Path screenshot;
    private final @Nullable List<Path> screenshots;
    private final int screenshotIndex;
    private final Path iconPath;
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);

    public ScreenshotViewerScreen(ScreenshotTexture icon, Path screenshot, Screen parent) {
        this(icon, screenshot, parent, null);
    }

    public ScreenshotViewerScreen(ScreenshotTexture icon, Path iconPath, Screen parent, @Nullable List<Path> screenshots) {
        super(Component.translatable("menu.snapper.viewer_menu"));
        this.parent = parent;
        this.iconPath = iconPath;

        BufferedImage image = null;

        try (InputStream stream = Files.newInputStream(iconPath)) {
            image = ImageIO.read(stream);
        } catch (IOException e) {
            Snapper.LOGGER.error("Failed to read image.", e);
            this.client.setScreen(parent);
        }

        this.image = icon;
        this.title = iconPath.getFileName().toString();

        this.imageWidth = image != null ? image.getWidth() : 0;
        this.imageHeight = image != null ? image.getHeight() : 0;

        this.screenshot = iconPath;
        this.screenshots = screenshots;

        this.screenshotIndex = this.screenshots != null ? this.screenshots.indexOf(this.screenshot) : -1;
    }

    @Override
    public void  onClose() {
        this.client.setScreen(this.parent);
    }

    @Override
    protected void init() {

        int firstRowButtonWidth = 74;

        // OPEN FOLDER

        Button folderButton = addRenderableWidget(Button.builder(
                                Component.translatable("button.snapper.folder"),
                                button -> Util.getPlatform().openFile(new File(client.gameDirectory, "screenshots"))
                        )
                        .width(100)
                        .build()
        );

        // OPEN IMAGE EXTERNALLY

        Button openButton = addRenderableWidget(Button.builder(
                Component.translatable("button.snapper.open"),
                button -> Util.getPlatform().openPath(this.iconPath)
        ).width(100).build());

        // EXIT PAGE

        Button doneButton = addRenderableWidget(Button.builder(
                CommonComponents.GUI_DONE,
                button -> this.onClose()
        ).width(100).build());

        // DELETE SCREENSHOT

        Button deleteButton = addRenderableWidget(Button.builder(
                Component.translatable("button.snapper.delete"),
                button -> ScreenshotActions.deleteScreenshot(this.screenshot, this.parent)
        ).width(firstRowButtonWidth).build());

        // RENAME SCREENSHOT

        Button renameButton = addRenderableWidget(Button.builder(
                Component.translatable("button.snapper.rename"),
                button -> {
                    if (this.screenshot != null)
                        client.setScreen(new ScreenshotRenameScreen(this.screenshot, this.parent));
                }
        ).width(firstRowButtonWidth).build());

        // COPY SCREENSHOT

        Button copyButton = addRenderableWidget(Button.builder(
                Component.translatable("button.snapper.copy"),
                button -> Snapper.getPlatformHelper().copyScreenshot(this.screenshot)
        ).width(firstRowButtonWidth).build());

        // UPLOAD SCREENSHOT

        Button uploadButton = addRenderableWidget(Button.builder(
                Component.translatable("button.snapper.upload"),
                button -> {
                    button.active = false;
                    ScreenshotUploading.upload(iconPath).thenRun(() -> button.active = true);
                }
        ).width(firstRowButtonWidth).build());

        if (SnapperUtil.isOfflineAccount()) {
            uploadButton.active = false;
            uploadButton.setTooltip(Tooltip.create(Component.translatable("button.snapper.upload.tooltip")));
        }

        LinearLayout verticalButtonLayout = LinearLayout.vertical().spacing(4);

        EqualSpacingLayout firstRowWidget = verticalButtonLayout.addChild(new EqualSpacingLayout(
                308,
                20,
                EqualSpacingLayout.Orientation.HORIZONTAL)
        );

        firstRowWidget.addChild(deleteButton);
        firstRowWidget.addChild(renameButton);
        firstRowWidget.addChild(copyButton);
        firstRowWidget.addChild(uploadButton);

        EqualSpacingLayout secondRowWidget = verticalButtonLayout.addChild(new EqualSpacingLayout(
                308,
                20,
                EqualSpacingLayout.Orientation.HORIZONTAL)
        );

        secondRowWidget.addChild(folderButton);
        secondRowWidget.addChild(openButton);
        secondRowWidget.addChild(doneButton);

        verticalButtonLayout.arrangeElements();
        FrameLayout.centerInRectangle(verticalButtonLayout, 0, this.height - 66, this.width, 64);

        layout.setHeaderHeight(46);
        layout.setFooterHeight(height - 68);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        this.drawMenuBackground(context);
        this.drawHeaderAndFooterSeparators(context);
        context.drawCenteredString(this.font, this.title, this.width / 2, 20, CommonColors.WHITE);

        int finalHeight = this.height - 50 - 68;
        float scaleFactor = (float) finalHeight / imageHeight;
        int finalWidth = (int) (imageWidth * scaleFactor);

        context.blit(
                RenderPipelines.GUI_TEXTURED,
                this.image.getTextureId(),
                (this.width / 2) - (finalWidth / 2), this.height - 70 - finalHeight,
                0, 0,
                finalWidth, finalHeight,
                finalWidth, finalHeight
        );

        if (screenshotIndex != -1 && screenshots != null) {
            context.drawCenteredString(
                    this.font,
                    "Screenshot %d/%d".formatted(screenshotIndex + 1, screenshots.size()),
                    this.width / 2,
                    30,
                    CommonColors.WHITE
            );
        }

        if (FabricLoader.getInstance().isDevelopmentEnvironment()) renderDebugInfo(context);
    }

    private void renderDebugInfo(GuiGraphics context) {
        context.pose().pushMatrix();
        int finalHeight = this.height - 50 - 68;
        float scaleFactor = (float) finalHeight / imageHeight;
        int finalWidth = (int) (imageWidth * scaleFactor);

        context.drawCenteredString(
                this.font,
                "Image Size: %dx%d".formatted(imageWidth, imageHeight),
                this.width / 2,
                40,
                CommonColors.WHITE
        );

        context.drawCenteredString(
                this.font,
                "Screen Size: %dx%d".formatted(this.width, this.height),
                this.width / 2,
                50,
                CommonColors.WHITE
        );

        context.drawCenteredString(this.font,
                "Scale Factor: %s".formatted(scaleFactor),
                this.width / 2,
                60,
                CommonColors.WHITE
        );

        context.drawCenteredString(
                this.font,
                "Scaled Size: %dx%d".formatted(finalWidth, finalHeight),
                this.width / 2,
                70,
                CommonColors.WHITE
        );
    }

    private void drawMenuBackground(GuiGraphics context) {
        context.blit(
                RenderPipelines.GUI_TEXTURED,
                this.client.level == null ?
                        MENU_DECOR_BACKGROUND_TEXTURE :
                        INWORLD_MENU_DECOR_BACKGROUND_TEXTURE,
                0,
                48,
                0,
                0,
                width,
                height - 68 - 48,
                32,
                32
        );
    }

    private void drawHeaderAndFooterSeparators(GuiGraphics context) {
        context.blit(
                RenderPipelines.GUI_TEXTURED,
                this.client.level == null ?
                        Screen.HEADER_SEPARATOR :
                        Screen.INWORLD_HEADER_SEPARATOR,
                0, layout.getHeaderHeight(),
                0, 0,
                width, 2,
                32, 2
        );

        context.blit(
                RenderPipelines.GUI_TEXTURED,
                this.client.level == null ?
                        Screen.FOOTER_SEPARATOR :
                        Screen.INWORLD_FOOTER_SEPARATOR,
                0, this.layout.getFooterHeight() - 2,
                0, 0,
                width, 2,
                32, 2
        );
    }
}
