package ca.worldwidepixel.screenshotutils.screen.screenshot;

import ca.worldwidepixel.screenshotutils.ScreenshotUtils;
import ca.worldwidepixel.screenshotutils.util.ScreenshotIcon;
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
    private int width = client.getWindow().getScaledWidth();
    private int height = client.getWindow().getScaledHeight();
    private int fullWidth = client.getWindow().getWidth();
    private int fullHeight = client.getWindow().getScaledHeight();
    private final int imageWidth;
    private final int imageHeight;

    protected ScreenshotViewerScreen(String title, ScreenshotIcon icon, Path path) throws IOException {
        super(Text.translatable("menu.screenshotutils.viewermenu"));
        BufferedImage img = ImageIO.read(new File(String.valueOf(path)));
        this.icon = icon;
        this.title = title;
        this.iconPath = path;
        //this.imageWidth = client.getGuiAtlasManager().getSprite(icon.getTextureId()).getContents().getWidth();
        this.imageWidth = img.getWidth();
        this.imageHeight = img.getHeight();
        //this.imageHeight = client.getGuiAtlasManager().getSprite(icon.getTextureId()).getContents().getHeight();
    }

    @Override
    public void close() {
        this.client.setScreen(new ScreenshotScreen());
    }

    @Override
    protected void init() {
        addDrawableChild(ButtonWidget.builder(Text.translatable("button.screenshotutils.view"), button -> {
                            Util.getOperatingSystem().open(this.iconPath);
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
        //context.getMatrices().push();
        int finalHeight = this.height - 48 - 48;
        float scaleFactor = (float) finalHeight / imageHeight;
        int finalWidth = (int) (imageWidth * scaleFactor);
        //context.getMatrices().scale(scaleFactor, scaleFactor, 1.0F);
        context.drawTexture(this.icon.getTextureId(), (this.width / 2) - (finalWidth / 2), this.height - 48 - finalHeight, 0, 0, finalWidth, finalHeight, finalWidth, finalHeight);
        //context.getMatrices().pop();
        //context.drawCenteredTextWithShadow(this.textRenderer, "Image height: " + this.imageHeight + " Image width: " + this.imageWidth, this.width / 2, 30, 16777215);
        //context.drawCenteredTextWithShadow(this.textRenderer, "Screen height: " + this.height + " Screen width: " + this.width, this.width / 2, 40, 16777215);
        //context.drawCenteredTextWithShadow(this.textRenderer, "Scale Factor: " + String.valueOf(scaleFactor), this.width / 2, 50, 16777215);
        //context.drawCenteredTextWithShadow(this.textRenderer, "Scaled image height: " + finalHeight + " Scaled image width: " + finalWidth, this.width / 2, 60, 16777215);
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        super.resize(client, width, height);
        this.width = client.getWindow().getScaledWidth();
        this.height = client.getWindow().getScaledHeight();
        this.fullWidth = client.getWindow().getWidth();
        this.fullHeight = client.getWindow().getScaledHeight();
    }
}
