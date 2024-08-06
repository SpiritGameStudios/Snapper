package dev.spiritstudios.snapper.gui;

import dev.spiritstudios.snapper.Snapper;
import dev.spiritstudios.snapper.util.ScreenshotIcon;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class ScreenshotViewerScreen extends Screen {
    private final MinecraftClient client = MinecraftClient.getInstance();
    private final ScreenshotIcon icon;
    private final Path iconPath;
    private final String title;
    private final int imageWidth;
    private final int imageHeight;
    private final Screen parent;

    public ScreenshotViewerScreen(String title, ScreenshotIcon icon, Path path, Screen parent) {
        super(Text.translatable("menu.snapper.viewermenu"));
        this.parent = parent;
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File(String.valueOf(path)));
        } catch (IOException e) {
            Snapper.LOGGER.error("Image failed to read.");
            this.client.setScreen(parent);
        }

        this.icon = icon;
        this.title = title;
        this.iconPath = path;
        this.imageWidth = img != null ? img.getWidth() : 0;
        this.imageHeight = img != null ? img.getHeight() : 0;
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    @Override
    protected void init() {
        addDrawableChild(ButtonWidget.builder(Text.translatable("button.snapper.open"), button -> Util.getOperatingSystem().open(this.iconPath))
                .dimensions(width / 2 - 150 - 4, height - 32, 150, 20)
                .build()
        );

        addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.close())
                .dimensions(width / 2 + 4, height - 32, 150, 20)
                .build()
        );
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 16777215);
        int finalHeight = this.height - 48 - 48;
        float scaleFactor = (float) finalHeight / imageHeight;
        int finalWidth = (int) (imageWidth * scaleFactor);

        context.drawTexture(this.icon.getTextureId(), (this.width / 2) - (finalWidth / 2), this.height - 48 - finalHeight, 0, 0, finalWidth, finalHeight, finalWidth, finalHeight);
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
}
