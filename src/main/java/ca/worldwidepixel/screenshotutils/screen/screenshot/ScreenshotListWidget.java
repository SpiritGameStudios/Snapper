package ca.worldwidepixel.screenshotutils.screen.screenshot;

import ca.worldwidepixel.screenshotutils.ScreenshotUtils;
import ca.worldwidepixel.screenshotutils.util.ScreenshotIcon;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.world.WorldListWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ScreenshotListWidget extends ElementListWidget<ScreenshotListWidget.Entry> {

    static final Identifier VIEW_TEXTURE = Identifier.of("screenshotutils", "screenshots/view");
    static final Identifier VIEW_HIGHLIGHTED_TEXTURE = Identifier.of("screenshotutils", "screenshots/view_highlighted");

    public ScreenshotListWidget(MinecraftClient client, int width, int height, int y, int itemHeight, File[] screenshots) throws IOException {
        super(client, width, height, y, itemHeight);
        for (int i = 0; i < screenshots.length; i++) {
            this.addEntryToTop(new Entry(screenshots, i, client));
        }
    }

    @Environment(EnvType.CLIENT)
    public static class Entry extends ElementListWidget.Entry<Entry> implements AutoCloseable {

        private final File[] screenshots;
        private final int screenshotIter;
        private final MinecraftClient client;
        private final ScreenshotIcon icon;
        private Path iconPath;
        private final String iconFileName;

        public Entry(final File[] screenshots, int iter, MinecraftClient client) {
            this.screenshots = screenshots;
            this.screenshotIter = iter;
            this.client = client;
            this.icon = ScreenshotIcon.forScreenshot(this.client.getTextureManager(), screenshots[iter].getName());
            //this.icon = WorldIcon.forWorld(this.client.getTextureManager(), level.getName());
            this.iconPath = screenshots[iter].toPath();
            this.iconFileName = this.screenshots[screenshotIter].getName();
            //this.validateIconPath();
            this.loadIcon();
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            String string = this.iconFileName;
            String string2 = "undefined";
            Path path = this.screenshots[screenshotIter].toPath();
            long l = 0;
            try {
                l = Files.readAttributes(path, BasicFileAttributes.class).creationTime().toMillis();
            } catch (IOException e) {
                //throw new RuntimeException(e);
                //ScreenshotUtils.LOGGER.error("SCREENSHOT MENU FAILED; FILE RENAMED/DELETED", e);
                ScreenshotUtils.LOGGER.error("FILE RENAMED/DELETED, RELOADING SCREEN");
                client.setScreen(new ScreenshotScreen());
            }
            if (l != -1L) {
                string2 = Text.translatable("text.screenshotutils.created").getString() + " " + WorldListWidget.DATE_FORMAT.format(Instant.ofEpochMilli(l));
            }

            if (StringUtils.isEmpty(string)) {
                string = I18n.translate("selectWorld.world") + " " + (index + 1);
            }

            context.drawText(this.client.textRenderer, string, x + 32 + 3, y + 1, 16777215, false);
            context.drawText(this.client.textRenderer, string2, x + 32 + 3, y + 9 + 3, Colors.GRAY, false);
            RenderSystem.enableBlend();
            context.drawTexture(this.icon.getTextureId(), x, y, 0.0F, 0.0F, 32, 32, 32, 32);
            RenderSystem.disableBlend();
                    if (this.client.options.getTouchscreen().getValue() || hovered) {
                        context.fill(x, y, x + 32, y + 32, -1601138544);
                        int i = mouseX - x;
                        boolean bl = i < 32;
                        Identifier identifier = bl ? ScreenshotListWidget.VIEW_HIGHLIGHTED_TEXTURE : ScreenshotListWidget.VIEW_TEXTURE;
                        context.drawGuiTexture(identifier, x, y, 32, 32);
                    }
        }

        private void loadIcon() {
            boolean bl = this.iconPath != null && Files.isRegularFile(this.iconPath, new LinkOption[0]);
            if (bl) {
                try {
                    InputStream inputStream = Files.newInputStream(this.iconPath);

                    try {
                        this.icon.load(NativeImage.read(inputStream));
                    } catch (Throwable var6) {
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (Throwable var5) {
                                var6.addSuppressed(var5);
                            }
                        }

                        throw var6;
                    }

                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (Throwable var7) {
                    ScreenshotUtils.LOGGER.error("Invalid icon for screenshot {}", screenshots[screenshotIter].getName(), var7);
                    this.iconPath = null;
                }
            } else {
                this.icon.destroy();
            }
        }

        @Override
        public List<? extends Element> children() {
            return List.of();
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            this.client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            try {
                this.client.setScreen(new ScreenshotViewerScreen(this.iconFileName, this.icon, this.iconPath));
            } catch (IOException e) {
                //throw new RuntimeException(e);
                this.client.setScreen(new TitleScreen());
            }
            return true;
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return List.of();
        }

        public void close() {
        }
    }
}