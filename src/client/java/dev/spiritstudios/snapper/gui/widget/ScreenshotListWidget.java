package dev.spiritstudios.snapper.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.spiritstudios.snapper.Snapper;
import dev.spiritstudios.snapper.gui.ScreenshotScreen;
import dev.spiritstudios.snapper.gui.ScreenshotViewerScreen;
import dev.spiritstudios.snapper.util.ScreenshotIcon;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
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
import net.minecraft.util.StringHelper;
import net.minecraft.util.Util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static dev.spiritstudios.snapper.Snapper.MODID;

public class ScreenshotListWidget extends AlwaysSelectedEntryListWidget<ScreenshotListWidget.Entry> {
    private static final Identifier VIEW_TEXTURE = Identifier.of(MODID, "screenshots/view");
    private static final Identifier VIEW_HIGHLIGHTED_TEXTURE = Identifier.of(MODID, "screenshots/view_highlighted");

    public CompletableFuture<List<ScreenshotEntry>> loadFuture;

    public ScreenshotListWidget(MinecraftClient client, int width, int height, int y, int itemHeight, ScreenshotListWidget previous) throws IOException {
        super(client, width, height, y, itemHeight);
        this.addEntry(new LoadingEntry(client));

        if (previous != null) this.loadFuture = previous.loadFuture;
        else this.loadFuture = load(client);

        this.loadFuture.thenAccept((entries) -> {
            this.clearEntries();
            entries.sort(Comparator.comparingLong(ScreenshotEntry::lastModified).reversed());
            entries.forEach(this::addEntry);
        });
    }

    @Override
    protected void clearEntries() {
        this.children().forEach(Entry::close);
        super.clearEntries();
    }

    public CompletableFuture<List<ScreenshotEntry>> load(MinecraftClient client) {
        return CompletableFuture.supplyAsync(() -> {
            List<File> screenshots = this.loadScreenshots();
            List<ScreenshotEntry> entries = new ArrayList<>();
            screenshots.parallelStream().forEach(file -> entries.add(new ScreenshotEntry(file, client)));
            return entries;
        });
    }

    private List<File> loadScreenshots() {
        File screenshotDir = new File(client.runDirectory, "screenshots");
        List<File> screenshots;

        File[] files = screenshotDir.listFiles();
        screenshots = new ArrayList<>(List.of(files == null ? new File[0] : files));

        screenshots.removeIf(file -> {
            if (Files.isDirectory(file.toPath())) return true;
            String fileType;

            try {
                fileType = Files.probeContentType(file.toPath());
            } catch (IOException e) {
                Snapper.LOGGER.error("Couldn't load screenshot list", e);
                return true;
            }

            return !Objects.equals(fileType, "image/png");
        });

        screenshots.sort(Comparator.comparingLong(File::lastModified).reversed());
        return screenshots;
    }

    public abstract static class Entry extends AlwaysSelectedEntryListWidget.Entry<Entry> implements AutoCloseable {
        public void close() {
        }
    }

    public static class LoadingEntry extends Entry implements AutoCloseable {
        private static final Text LOADING_LIST_TEXT = Text.translatable("text.snapper.loading");
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
            if (this.client.currentScreen == null) throw new IllegalStateException("How did we get here?");

            context.drawText(
                    this.client.textRenderer,
                    LOADING_LIST_TEXT,
                    (this.client.currentScreen.width - this.client.textRenderer.getWidth(LOADING_LIST_TEXT)) / 2,
                    y + (entryHeight - 9) / 2,
                    0xFFFFFF,
                    false
            );

            String loadString = LoadingDisplay.get(Util.getMeasuringTimeMs());

            context.drawText(
                    this.client.textRenderer,
                    loadString,
                    (this.client.currentScreen.width - this.client.textRenderer.getWidth(loadString)) / 2,
                    y + (entryHeight - 9) / 2 + 9,
                    Colors.GRAY,
                    false
            );
        }
    }

    public static class ScreenshotEntry extends Entry implements AutoCloseable {
        public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withZone(ZoneId.systemDefault());
        public final long lastModified;
        private final MinecraftClient client;
        private final ScreenshotIcon icon;
        private final String iconFileName;
        private Path iconPath;


        public ScreenshotEntry(File screenshot, MinecraftClient client) {
            this.client = client;
            this.icon = ScreenshotIcon.forScreenshot(this.client.getTextureManager(), screenshot.getName());
            this.iconPath = screenshot.toPath();
            this.iconFileName = screenshot.getName();
            this.lastModified = screenshot.lastModified();
            this.loadIcon();
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            String fileName = this.iconFileName;
            String creationString = "undefined";

            long creationTime = 0;
            try {
                creationTime = Files.readAttributes(iconPath, BasicFileAttributes.class).creationTime().toMillis();
            } catch (IOException e) {
                Snapper.LOGGER.error("FILE RENAMED/DELETED, RELOADING SCREEN");
                client.setScreen(new ScreenshotScreen());
            }

            if (creationTime != -1L)
                creationString = Text.translatable("text.snapper.created").getString() + " " + DATE_FORMAT.format(Instant.ofEpochMilli(creationTime));

            if (StringHelper.isEmpty(fileName))
                fileName = I18n.translate("text.snapper.generic") + " " + (index + 1);

            context.drawText(
                    this.client.textRenderer,
                    fileName,
                    x + 32 + 3,
                    y + 1,
                    16777215,
                    false
            );

            context.drawText(
                    this.client.textRenderer,
                    creationString,
                    x + 35,
                    y + 12,
                    Colors.GRAY,
                    false
            );

            if (this.icon != null) {
                RenderSystem.enableBlend();
                context.drawTexture(
                        this.icon.getTextureId(),
                        x,
                        y,
                        32,
                        32,
                        //centered
                        (icon.getHeight()) / 3.0F + 32,
                        0,
                        icon.getHeight(),
                        icon.getHeight(),
                        icon.getWidth(),
                        icon.getHeight()
                );
                RenderSystem.disableBlend();
            }
            if (this.client.options.getTouchscreen().getValue() || hovered) {
                context.fill(x, y, x + 32, y + 32, 0xA0909090);
                context.drawGuiTexture(
                        mouseX - x < 32 && this.icon != null ? ScreenshotListWidget.VIEW_HIGHLIGHTED_TEXTURE : ScreenshotListWidget.VIEW_TEXTURE,
                        x,
                        y,
                        32,
                        32
                );
            }
        }

        private void loadIcon() {
            CompletableFuture.runAsync(() -> {
                if (this.iconPath == null || !Files.isRegularFile(this.iconPath)) {
                    this.icon.destroy();
                    return;
                }

                try (InputStream inputStream = Files.newInputStream(this.iconPath)) {
                    this.icon.load(NativeImage.read(inputStream));
                } catch (IOException error) {
                    Snapper.LOGGER.error("Invalid icon for screenshot {}", iconFileName, error);
                    this.iconPath = null;
                }
            });
        }

        @Override
        public Text getNarration() {
            return Text.literal(this.iconFileName);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (this.icon == null) return false;
            this.client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));

            try {
                this.client.setScreen(new ScreenshotViewerScreen(this.iconFileName, this.icon, this.iconPath));
            } catch (IOException e) {
                this.client.setScreen(new TitleScreen());
            }

            return true;
        }

        @Override
        public void close() {
            this.icon.close();
        }

        public long lastModified() {
            return lastModified;
        }
    }
}