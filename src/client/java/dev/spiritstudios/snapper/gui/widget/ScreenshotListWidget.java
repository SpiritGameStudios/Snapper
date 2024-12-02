package dev.spiritstudios.snapper.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.spiritstudios.snapper.Snapper;
import dev.spiritstudios.snapper.gui.screen.ScreenshotScreen;
import dev.spiritstudios.snapper.gui.screen.ScreenshotViewerScreen;
import dev.spiritstudios.snapper.util.ScreenshotActions;
import dev.spiritstudios.snapper.util.ScreenshotImage;
import dev.spiritstudios.specter.api.core.exception.UnreachableException;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.LoadingDisplay;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.input.KeyCodes;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringHelper;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

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
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ScreenshotListWidget extends AlwaysSelectedEntryListWidget<ScreenshotListWidget.Entry> {
    private static final Identifier VIEW_TEXTURE = Snapper.id("screenshots/view");
    private static final Identifier VIEW_HIGHLIGHTED_TEXTURE = Snapper.id("screenshots/view_highlighted");

    private final Screen parent;

    public final CompletableFuture<List<ScreenshotEntry>> loadFuture;

    public ScreenshotListWidget(MinecraftClient client, int width, int height, int y, int itemHeight, @Nullable ScreenshotListWidget previous, Screen parent) {
        super(client, width, height, y, itemHeight);

        this.parent = parent;
        this.addEntry(new LoadingEntry(client));

        this.loadFuture = previous != null ? previous.loadFuture : load(client);

        this.loadFuture.thenAccept(entries -> {
            this.clearEntries();
            entries.sort(Comparator.comparingLong(ScreenshotEntry::lastModified).reversed());
            entries.forEach(this::addEntry);

            if (entries.isEmpty()) {
                this.addEntry(new EmptyEntry(client));
            }
        });
    }

    @Override
    protected void clearEntries() {
        this.children().forEach(Entry::close);
        super.clearEntries();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (KeyCodes.isToggle(keyCode)) {
            Entry entry = this.getSelectedOrNull();
            if (entry instanceof ScreenshotEntry screenshotEntry) return screenshotEntry.click();
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public CompletableFuture<List<ScreenshotEntry>> load(MinecraftClient client) {
        return CompletableFuture.supplyAsync(() -> {
            List<File> screenshots = ScreenshotActions.getScreenshots(client);
            return screenshots.parallelStream()
                    .map(file -> new ScreenshotEntry(file, client, parent))
                    .collect(Collectors.toList());
        });
    }

    private void setEntrySelected(@Nullable ScreenshotEntry entry) {
        super.setSelected(entry);
        if (this.parent instanceof ScreenshotScreen screenshotScreen)
            screenshotScreen.imageSelected(entry);
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

    public static class EmptyEntry extends Entry implements AutoCloseable {
        private static final Text EMPTY_LIST_TEXT = Text.translatable("text.snapper.empty");
        private final MinecraftClient client;

        public EmptyEntry(MinecraftClient client) {
            this.client = client;
        }

        @Override
        public Text getNarration() {
            return EMPTY_LIST_TEXT;
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            if (this.client.currentScreen == null) throw new UnreachableException();

            context.drawText(
                    this.client.textRenderer,
                    EMPTY_LIST_TEXT,
                    (this.client.currentScreen.width - this.client.textRenderer.getWidth(EMPTY_LIST_TEXT)) / 2,
                    y + entryHeight / 2,
                    0xFFFFFF,
                    false
            );
        }
    }

    public class ScreenshotEntry extends Entry implements AutoCloseable {
        public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter
                .ofLocalizedDateTime(FormatStyle.SHORT)
                .withZone(ZoneId.systemDefault());

        public final long lastModified;
        private final MinecraftClient client;
        public final ScreenshotImage icon;
        public final String iconFileName;
        public Path iconPath;
        public final Screen screenParent;
        private long time;
        public final File screenshot;

        public ScreenshotEntry(File screenshot, MinecraftClient client, Screen parent) {
            this.client = client;
            this.screenParent = parent;
            this.icon = ScreenshotImage.forScreenshot(this.client.getTextureManager(), screenshot.getName());
            this.iconPath = Path.of(screenshot.getPath());
            this.iconFileName = screenshot.getName();
            this.lastModified = screenshot.lastModified();
            this.screenshot = screenshot;
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
                client.setScreen(new ScreenshotScreen(screenParent));
            }

            if (creationTime != -1L)
                creationString = Text.translatable("text.snapper.created").getString() + " " + DATE_FORMAT.format(Instant.ofEpochMilli(creationTime));

            if (StringHelper.isEmpty(fileName))
                fileName = Text.translatable("text.snapper.generic") + " " + (index + 1);

            context.drawText(
                    this.client.textRenderer,
                    fileName,
                    x + 32 + 3,
                    y + 1,
                    0xFFFFFF,
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
                } catch (Throwable error) {
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
            ScreenshotListWidget.this.setEntrySelected(this);

            if (!(mouseX - (double) ScreenshotListWidget.this.getRowLeft() <= 32.0) && Util.getMeasuringTimeMs() - this.time >= 250L) {
                this.time = Util.getMeasuringTimeMs();
                return super.mouseClicked(mouseX, mouseY, button);
            }

            return click();
        }

        public boolean click() {
            if (this.icon == null) return false;
            this.client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));

            this.client.setScreen(new ScreenshotViewerScreen(this.icon, this.screenshot, this.screenParent));
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