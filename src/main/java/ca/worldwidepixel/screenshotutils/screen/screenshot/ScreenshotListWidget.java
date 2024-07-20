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
import net.minecraft.client.gui.screen.LoadingDisplay;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.level.storage.LevelSummary;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class ScreenshotListWidget extends AlwaysSelectedEntryListWidget<ScreenshotListWidget.Entry> {

    static final Identifier VIEW_TEXTURE = Identifier.of("screenshotutils", "screenshots/view");
    static final Identifier VIEW_HIGHLIGHTED_TEXTURE = Identifier.of("screenshotutils", "screenshots/view_highlighted");
    private final LoadingEntry loadingEntry;
    private CompletableFuture<File[]> screenshotsFuture;
    private File[] screenshots;

    public ScreenshotListWidget(MinecraftClient client, int width, int height, int y, int itemHeight) throws IOException {
        super(client, width, height, y, itemHeight);
        this.loadingEntry = new LoadingEntry(client);
        this.screenshotsFuture = this.loadScreenshots();
        this.show(this.tryGet());
    }

    private File[] tryGet() {
        try {
            return this.screenshotsFuture.getNow(null);
        } catch (CancellationException | CompletionException e) {
            return null;
        }
    }

    private void show(@Nullable File[] screenshots) {
        if (screenshots == null) {
            this.showLoading();
        } else {
            this.showContent(screenshots);
        }
        this.screenshots = screenshots;
    }

    private void showContent(File[] shots) {
        this.clearEntries();
        for (int i = 0; i < shots.length; i++) {
            this.addEntry(new ScreenshotEntry(shots, i, client));
        }
    }

    private void showLoading() {
        this.clearEntries();
        this.addEntry(this.loadingEntry);
    }

    private CompletableFuture<File[]> loadScreenshots() {
        File screenshotDir = new File(client.runDirectory, "screenshots");
        File[] screenshots;
        try {
            screenshots = screenshotDir.listFiles();
            Arrays.sort(screenshots, (f1, f2) -> Long.valueOf(f1.lastModified()).compareTo(f2.lastModified()));
            for(int i = 0; i < screenshots.length / 2; i++)
            {
                File temp = screenshots[i];
                screenshots[i] = screenshots[screenshots.length - i - 1];
                screenshots[screenshots.length - i - 1] = temp;
            }
            for (File screenshot : screenshots) {
                if (Files.isDirectory(screenshot.toPath())) {
                    screenshots = ArrayUtils.removeElement(screenshots, screenshot);
                    continue;
                }
                String fileType = Files.probeContentType(screenshot.toPath());
                ScreenshotUtils.LOGGER.info(fileType);
                if (Objects.equals(fileType, "image/png")) {
                    continue;
                } else {
                    screenshots = ArrayUtils.removeElement(screenshots, screenshot);
                }
            }
            return CompletableFuture.completedFuture(screenshots);
        } catch (IOException e) {
            ScreenshotUtils.LOGGER.error("Couldn't load screenshot list", (Throwable) e);
            //this.showUnableToLoadScreen(var3.getMessageText());
            return CompletableFuture.completedFuture(screenshotDir.listFiles());
        }
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        File[] shots = this.tryGet();
        if (shots != this.screenshots) {
            this.show(shots);
        }

        super.renderWidget(context, mouseX, mouseY, delta);
    }

    @Environment(EnvType.CLIENT)
    public abstract static class Entry extends AlwaysSelectedEntryListWidget.Entry<Entry> implements AutoCloseable {
        public void close() {
        }
    }

    @Environment(EnvType.CLIENT)
    public static class LoadingEntry extends Entry implements AutoCloseable {

        private static final Text LOADING_LIST_TEXT = Text.translatable("text.screenshotutils.loading");
        private final MinecraftClient client;

        public LoadingEntry(MinecraftClient client) {
            this.client = client;
        }

        @Override
        public Text getNarration() {
            return LOADING_LIST_TEXT;
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int i = (this.client.currentScreen.width - this.client.textRenderer.getWidth(LOADING_LIST_TEXT)) / 2;
            int j = y + (entryHeight - 9) / 2;
            context.drawText(this.client.textRenderer, LOADING_LIST_TEXT, i, j, 16777215, false);
            String string = LoadingDisplay.get(Util.getMeasuringTimeMs());
            int k = (this.client.currentScreen.width - this.client.textRenderer.getWidth(string)) / 2;
            int l = j + 9;
            context.drawText(this.client.textRenderer, string, k, l, Colors.GRAY, false);
        }
    }

    @Environment(EnvType.CLIENT)
    public static class ScreenshotEntry extends Entry implements AutoCloseable {

        private final File[] screenshots;
        private final int screenshotIter;
        private final MinecraftClient client;
        private final ScreenshotIcon icon;
        private Path iconPath;
        private final String iconFileName;

        public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withZone(ZoneId.systemDefault());


        public ScreenshotEntry(final File[] screenshots, int iter, MinecraftClient client) {
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
                string2 = Text.translatable("text.screenshotutils.created").getString() + " " + DATE_FORMAT.format(Instant.ofEpochMilli(l));
            }

            if (StringUtils.isEmpty(string)) {
                string = I18n.translate("text.screenshotutils.generic") + " " + (index + 1);
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
        public Text getNarration() {
            return Text.literal(this.iconFileName);
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

        public void close() {
        }
    }
}