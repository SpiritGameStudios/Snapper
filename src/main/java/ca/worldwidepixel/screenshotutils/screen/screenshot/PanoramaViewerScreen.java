package ca.worldwidepixel.screenshotutils.screen.screenshot;

import ca.worldwidepixel.screenshotutils.ScreenshotUtils;
import ca.worldwidepixel.screenshotutils.util.ScreenshotIcon;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.CubeMapRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.RotatingCubeMapRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class PanoramaViewerScreen extends Screen {

    private static final MinecraftClient client = MinecraftClient.getInstance();
    //private final ScreenshotIcon icon;
    private final Path iconPath;
    private final String title;
    private int width = client.getWindow().getScaledWidth();
    private int height = client.getWindow().getScaledHeight();
    //private final int imageWidth;
    //private final int imageHeight;
    private boolean showPanorama = false;
    //private List<File> panorama;
    protected static final CubeMapRenderer PANORAMA_RENDERER = new CubeMapRenderer(Identifier.ofVanilla("screenshots" + "/panorama/panorama"));
    protected static final RotatingCubeMapRenderer PANORAMA_RENDERER_CUBE;

    protected PanoramaViewerScreen(String title) throws IOException {
        super(Text.translatable("menu.screenshotutils.viewermenu"));
        //BufferedImage img = ImageIO.read(new File(String.valueOf(path)));
        //this.icon = icon;
        this.title = title;
        this.iconPath = new File(client.runDirectory + "screenshots/panorama").toPath();
        //this.imageWidth = img.getWidth();
        //this.imageHeight = img.getHeight();
        this.load();
    }

    static {
        PANORAMA_RENDERER_CUBE = new RotatingCubeMapRenderer(PANORAMA_RENDERER);
    }

    private void load() {
        List<File> panorama = this.loadPanorama();
        for (File face : panorama) {
            ScreenshotIcon icon = ScreenshotIcon.forPanoramaFace(client.getTextureManager(), face.getName());
            this.loadIcon(icon, face.getName(), Path.of(face.getPath()));
        }
    }

    private void loadIcon(ScreenshotIcon icon, String fileName, Path filePath) {
        CompletableFuture.runAsync(() -> {
            if (filePath == null || !Files.isRegularFile(filePath)) {
                icon.destroy();
                return;
            }

            try (InputStream inputStream = Files.newInputStream(filePath)) {
                icon.load(NativeImage.read(inputStream));
            } catch (IOException error) {
                ScreenshotUtils.LOGGER.error("Invalid face for panorama {}", fileName, error);
            }
        });
    }

    private List<File> loadPanorama() {
        File panoramaDir = new File(client.runDirectory, "screenshots/panorama");
        List<File> panoramaFaces;
        if (!Files.exists(panoramaDir.toPath())) {
            this.showPanorama = false;
            return null;
        } else {
            File[] faceFiles = panoramaDir.listFiles();
            panoramaFaces = new ArrayList<>(List.of(faceFiles == null ? new File[0] : faceFiles));
            panoramaFaces.removeIf(file -> {
                if (Files.isDirectory(file.toPath())) return true;
                String fileType;

                try {
                    fileType = Files.probeContentType(file.toPath());
                } catch (IOException e) {
                    ScreenshotUtils.LOGGER.error("Couldn't load panorama list", e);
                    return true;
                }

                return !Objects.equals(fileType, "image/png");
            });
            this.showPanorama = true;
            return panoramaFaces;
        }
    }

    @Override
    public void close() {
        this.client.setScreen(new ScreenshotScreen());
    }

    @Override
    protected void init() {
        addDrawableChild(ButtonWidget.builder(Text.translatable("button.screenshotutils.view"), button -> Util.getOperatingSystem().open(this.iconPath))
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
        this.renderPanoramaBackground(context, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 16777215);
        //context.getMatrices().push();
        //int finalHeight = this.height - 48 - 48;
        //float scaleFactor = (float) finalHeight / imageHeight;
        //int finalWidth = (int) (imageWidth * scaleFactor);
        //context.getMatrices().scale(scaleFactor, scaleFactor, 1.0F);
        //context.drawTexture(this.icon.getTextureId(), (this.width / 2) - (finalWidth / 2), this.height - 48 - finalHeight, 0, 0, finalWidth, finalHeight, finalWidth, finalHeight);
        //context.getMatrices().pop();
        //context.drawCenteredTextWithShadow(this.textRenderer, "Image height: " + this.imageHeight + " Image width: " + this.imageWidth, this.width / 2, 30, 16777215);
        //context.drawCenteredTextWithShadow(this.textRenderer, "Screen height: " + this.height + " Screen width: " + this.width, this.width / 2, 40, 16777215);
        //context.drawCenteredTextWithShadow(this.textRenderer, "Scale Factor: " + String.valueOf(scaleFactor), this.width / 2, 50, 16777215);
        //context.drawCenteredTextWithShadow(this.textRenderer, "Scaled image height: " + finalHeight + " Scaled image width: " + finalWidth, this.width / 2, 60, 16777215);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
    }

    @Override
    protected void renderPanoramaBackground(DrawContext context, float delta) {
        PANORAMA_RENDERER_CUBE.render(context, this.width, this.height, 1, delta);
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        super.resize(client, width, height);
        this.width = client.getWindow().getScaledWidth();
        this.height = client.getWindow().getScaledHeight();
    }
}
