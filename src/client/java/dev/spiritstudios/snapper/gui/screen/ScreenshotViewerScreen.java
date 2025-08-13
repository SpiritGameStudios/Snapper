package dev.spiritstudios.snapper.gui.screen;

import dev.spiritstudios.snapper.Snapper;
import dev.spiritstudios.snapper.util.DynamicTexture;
import dev.spiritstudios.snapper.util.ScreenshotActions;
import dev.spiritstudios.snapper.util.SnapperUtil;
import dev.spiritstudios.snapper.util.uploading.ScreenshotUploading;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.AxisGridWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.lwjgl.glfw.GLFW.*;

public class ScreenshotViewerScreen extends Screen {
    private static final Identifier MENU_DECOR_BACKGROUND_TEXTURE = Identifier.ofVanilla("textures/gui/menu_list_background.png");
    private static final Identifier INWORLD_MENU_DECOR_BACKGROUND_TEXTURE = Identifier.ofVanilla("textures/gui/inworld_menu_list_background.png");

    private final MinecraftClient client = MinecraftClient.getInstance();
    private final DynamicTexture image;
    private final String title;
    private final int imageWidth;
    private final int imageHeight;
    private final Screen parent;
    private final Path screenshot;
    private final @Nullable List<Path> screenshots;
    private final int screenshotIndex;
    private final Path iconPath;

    public ScreenshotViewerScreen(DynamicTexture icon, Path screenshot, Screen parent) {
        this(icon, screenshot, parent, null);
    }

    public ScreenshotViewerScreen(DynamicTexture icon, Path iconPath, Screen parent, @Nullable List<Path> screenshots) {
        super(Text.translatable("menu.snapper.viewer_menu"));
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
    public void close() {
        this.client.setScreen(this.parent);
    }

    @Override
    protected void init() {

        int firstRowButtonWidth = 74;

        // OPEN FOLDER

        ButtonWidget folderButton = addDrawableChild(ButtonWidget.builder(
                                Text.translatable("button.snapper.folder"),
                                button -> Util.getOperatingSystem().open(new File(client.runDirectory, "screenshots"))
                        )
                        .width(100)
                        .build()
        );

        // OPEN IMAGE EXTERNALLY

        ButtonWidget openButton = addDrawableChild(ButtonWidget.builder(
                Text.translatable("button.snapper.open"),
                button -> Util.getOperatingSystem().open(this.iconPath)
        ).width(100).build());

        // EXIT PAGE

        ButtonWidget doneButton = addDrawableChild(ButtonWidget.builder(
                ScreenTexts.DONE,
                button -> this.close()
        ).width(100).build());

        // DELETE SCREENSHOT

        ButtonWidget deleteButton = addDrawableChild(ButtonWidget.builder(
                Text.translatable("button.snapper.delete"),
                button -> ScreenshotActions.deleteScreenshot(this.screenshot, this.parent)
        ).width(firstRowButtonWidth).build());

        // RENAME SCREENSHOT

        ButtonWidget renameButton = addDrawableChild(ButtonWidget.builder(
                Text.translatable("button.snapper.rename"),
                button -> {
                    if (this.screenshot != null)
                        client.setScreen(new RenameScreenshotScreen(this.screenshot, this.parent));
                }
        ).width(firstRowButtonWidth).build());

        // COPY SCREENSHOT

        ButtonWidget copyButton = addDrawableChild(ButtonWidget.builder(
                Text.translatable("button.snapper.copy"),
                button -> Snapper.getPlatformHelper().copyScreenshot(this.screenshot)
        ).width(firstRowButtonWidth).build());

        // UPLOAD SCREENSHOT

        ButtonWidget uploadButton = addDrawableChild(ButtonWidget.builder(
                Text.translatable("button.snapper.upload"),
                button -> {
                    button.active = false;
                    ScreenshotUploading.upload(iconPath).thenRun(() -> button.active = true);
                }
        ).width(firstRowButtonWidth).build());

        if (SnapperUtil.isOfflineAccount()) {
            uploadButton.active = false;
            uploadButton.setTooltip(Tooltip.of(Text.translatable("button.snapper.upload.tooltip")));
        }

        DirectionalLayoutWidget verticalButtonLayout = DirectionalLayoutWidget.vertical().spacing(4);

        AxisGridWidget firstRowWidget = verticalButtonLayout.add(new AxisGridWidget(
                308,
                20,
                AxisGridWidget.DisplayAxis.HORIZONTAL)
        );

        firstRowWidget.add(deleteButton);
        firstRowWidget.add(renameButton);
        firstRowWidget.add(copyButton);
        firstRowWidget.add(uploadButton);

        AxisGridWidget secondRowWidget = verticalButtonLayout.add(new AxisGridWidget(
                308,
                20,
                AxisGridWidget.DisplayAxis.HORIZONTAL)
        );

        secondRowWidget.add(folderButton);
        secondRowWidget.add(openButton);
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
                RenderLayer::getGuiTextured,
                this.image.getTextureId(),
                (this.width / 2) - (finalWidth / 2), this.height - 68 - finalHeight,
                0, 0,
                finalWidth, finalHeight,
                finalWidth, finalHeight
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
        context.drawTexture(
                RenderLayer::getGuiTextured,
                this.client.world == null ?
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

    private void drawHeaderAndFooterSeparators(DrawContext context) {
        context.drawTexture(
                RenderLayer::getGuiTextured,
                this.client.world == null ?
                        Screen.HEADER_SEPARATOR_TEXTURE :
                        Screen.INWORLD_HEADER_SEPARATOR_TEXTURE,
                0, 48 - 2,
                0, 0,
                width, 2,
                32, 2
        );

        context.drawTexture(
                RenderLayer::getGuiTextured,
                this.client.world == null ?
                        Screen.FOOTER_SEPARATOR_TEXTURE :
                        Screen.INWORLD_FOOTER_SEPARATOR_TEXTURE,
                0, height - 68,
                0, 0,
                width, 2,
                32, 2
        );
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.screenshotIndex == -1 || this.screenshots == null)
            return super.keyPressed(keyCode, scanCode, modifiers);

        Path imagePath = switch (keyCode) {
            case GLFW_KEY_LEFT -> this.screenshotIndex >= 1 ?
                    screenshots.get(screenshotIndex - 1) :
                    screenshots.getLast();
            case GLFW_KEY_RIGHT -> this.screenshotIndex < this.screenshots.size() - 1 ?
                    screenshots.get(screenshotIndex + 1) :
                    screenshots.getFirst();
            default -> null;
        };

        if (imagePath == null) return super.keyPressed(keyCode, scanCode, modifiers);
		CompletableFuture.supplyAsync(() -> DynamicTexture.createScreenshot(client.getTextureManager(), imagePath), Util.getIoWorkerExecutor())
				.thenAccept(texture -> {
					texture.ifPresent(dynamicTexture -> client.submit(() -> {
						client.setScreen(new ScreenshotViewerScreen(
								dynamicTexture, imagePath,
								this.parent,
								this.screenshots
						));
						dynamicTexture.load();
					}));
				});

        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
