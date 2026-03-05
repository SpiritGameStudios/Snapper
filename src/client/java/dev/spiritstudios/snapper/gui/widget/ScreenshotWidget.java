package dev.spiritstudios.snapper.gui.widget;

import com.mojang.blaze3d.platform.InputConstants;
import dev.spiritstudios.snapper.Snapper;
import dev.spiritstudios.snapper.SnapperConfig;
import dev.spiritstudios.snapper.gui.screen.ScreenshotScreen;
import dev.spiritstudios.snapper.gui.screen.ScreenshotViewerScreen;
import dev.spiritstudios.snapper.util.SafeFiles;
import dev.spiritstudios.snapper.util.ScreenshotActions;
import dev.spiritstudios.snapper.util.ScreenshotTexture;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.LoadingDotsText;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CommonColors;
import net.minecraft.util.StringUtil;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class ScreenshotWidget extends ObjectSelectionList<ScreenshotWidget.Entry> {
    protected static final ResourceLocation VIEW_SPRITE = Snapper.id("screenshots/view");
    protected static final ResourceLocation VIEW_HIGHLIGHTED_SPRITE = Snapper.id("screenshots/view_highlighted");
    protected static final ResourceLocation GRID_SELECTION_BACKGROUND_TEXTURE = Snapper.id("textures/gui/grid_selection_background.png");

    protected final Screen parent;
    public final CompletableFuture<List<ScreenshotTexture>> loadFuture;

    public static ScreenshotWidget create(
            Minecraft client,
            int width, int height,
            int y, @Nullable ScreenshotWidget previous,
            Screen parent
    ) {
        if (SnapperConfig.HOLDER.get().viewMode() == ScreenshotScreen.ViewMode.GRID) {
            return new ScreenshotGridWidget(client, width, height, y, previous, parent);
        } else {
            return new ScreenshotListWidget(client, width, height, y, previous, parent);
        }
    }

    public ScreenshotWidget(
            Minecraft client,
            int width, int height,
            int y, int itemHeight,
            @Nullable ScreenshotWidget previous,
            Screen parent
    ) {
        super(client, width, height, y, itemHeight);

        this.parent = parent;
        this.addEntry(new ScreenshotWidget.LoadingEntry(client));

        this.loadFuture = previous != null ? previous.loadFuture : load(client);

        this.loadFuture.thenAccept(textures -> {
            this.clearEntries();

            if (textures.isEmpty()) {
                this.addEntry(new ScreenshotWidget.EmptyEntry(client));
            } else {
                for (ScreenshotTexture texture : textures) {
                    addEntry(createEntry(texture));
                }
            }

            repositionEntries();
        });

        repositionEntries();
    }

    @Override
    protected void clearEntries() {
        this.children().forEach(Entry::close);
        super.clearEntries();
    }

    protected void setEntrySelected(@Nullable ScreenshotEntry entry) {
        super.setSelected(entry);
        if (this.parent instanceof ScreenshotScreen screenshotScreen) {
            screenshotScreen.imageSelected(entry);
        }
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        if (input.key() == InputConstants.KEY_RETURN) {
            Entry entry = this.getSelected();
            if (entry instanceof ScreenshotEntry screenshotEntry) return screenshotEntry.click();
        }

        return super.keyPressed(input);
    }


    protected abstract ScreenshotEntry createEntry(ScreenshotTexture icon);

    public CompletableFuture<List<ScreenshotTexture>> load(Minecraft client) {
        return CompletableFuture.supplyAsync(() -> {
                    List<Path> screenshots = ScreenshotActions.getScreenshots();

                    return screenshots.parallelStream()
                            .flatMap(path -> ScreenshotTexture.createScreenshot(client.getTextureManager(), path).stream())
                            .peek(screenshotImage -> screenshotImage.load()
                                    .exceptionally(throwable -> {
                                        Snapper.LOGGER.error("An error occurred while loading the screenshot list", throwable);
                                        return null;
                                    }))
                            .sorted(Comparator.<ScreenshotTexture, FileTime>comparing(texture ->
                                    SafeFiles.getLastModifiedTime(texture.getPath()).orElse(FileTime.fromMillis(0L))).reversed())
                            .toList();
                })
                .exceptionally(throwable -> {
                    Snapper.LOGGER.error("An error occurred while loading the screenshot list", throwable);
                    return Collections.emptyList();
                });
    }

    public abstract static class Entry extends ObjectSelectionList.Entry<Entry> implements AutoCloseable {
        public void close() {
        }
    }

    public static class LoadingEntry extends Entry implements AutoCloseable {
        private static final Component LOADING_LIST_TEXT = Component.translatable("text.snapper.loading");
        private final Minecraft client;

        public LoadingEntry(Minecraft client) {
            this.client = client;
        }

        @Override
        public @NonNull Component getNarration() {
            return LOADING_LIST_TEXT;
        }

        @Override
        public void renderContent(GuiGraphics context, int mouseX, int mouseY, boolean isHovering, float partialTick) {
            if (this.client.screen == null) throw new IllegalStateException();

            context.drawString(
                    this.client.font,
                    LOADING_LIST_TEXT,
                    (this.client.screen.width - this.client.font.width(LOADING_LIST_TEXT)) / 2,
                    getY() + (getHeight() - 9) / 2,
                    CommonColors.WHITE,
                    false
            );

            String loadString = LoadingDotsText.get(Util.getMillis());

            context.drawString(
                    this.client.font,
                    loadString,
                    (this.client.screen.width - this.client.font.width(loadString)) / 2,
                    getY() + (getHeight() - 9) / 2 + 9,
                    CommonColors.GRAY,
                    false
            );
        }
    }

    public static class EmptyEntry extends Entry implements AutoCloseable {
        private static final Component EMPTY_LIST_TEXT = Component.translatable("text.snapper.empty");
        private static final Component EMPTY_CUSTOM_LIST_TEXT = Component.translatable("text.snapper.empty.custom");
        private final Minecraft minecraft;

        public EmptyEntry(Minecraft minecraft) {
            this.minecraft = minecraft;
        }

        @Override
        public @NonNull Component getNarration() {
            return EMPTY_LIST_TEXT;
        }

        @Override
        public void renderContent(GuiGraphics context, int mouseX, int mouseY, boolean isHovering, float partialTick) {
            if (this.minecraft.screen == null) throw new IllegalStateException();

            context.drawString(
                    this.minecraft.font,
                    EMPTY_LIST_TEXT,
                    (this.minecraft.screen.width - this.minecraft.font.width(EMPTY_LIST_TEXT)) / 2,
                    getY() + getHeight() / 2,
                    CommonColors.WHITE,
                    false
            );

            context.drawString(
                    this.minecraft.font,
                    EMPTY_CUSTOM_LIST_TEXT,
                    (this.minecraft.screen.width - this.minecraft.font.width(EMPTY_CUSTOM_LIST_TEXT)) / 2,
                    getY() + getHeight() / 2 + 10,
                    CommonColors.WHITE,
                    false
            );
        }
    }

    public abstract class ScreenshotEntry extends Entry implements AutoCloseable {
        public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter
                .ofLocalizedDateTime(FormatStyle.SHORT)
                .withZone(ZoneId.systemDefault());

        public final FileTime lastModified;
        public final ScreenshotTexture icon;
        public final String iconFileName;
        public final Screen screenParent;
        protected long time;
        protected boolean clickThroughHovered = false;
        protected final int index;

        public ScreenshotEntry(ScreenshotTexture icon) {
            this.screenParent = parent;
            this.icon = icon;
            this.iconFileName = icon.getPath().getFileName().toString();
            this.lastModified = SafeFiles.getLastModifiedTime(icon.getPath()).orElse(FileTime.fromMillis(0L));
            this.index = children().indexOf(this);
        }

        public void renderMetadata(GuiGraphics context, int mouseX, int mouseY, boolean hovered, float deltaTick) {
            String fileName = this.iconFileName;

            int centreX = getX() + getWidth() / 2;
            int centreY = getY() + getHeight() / 2;

            if (StringUtil.isNullOrEmpty(fileName))
                fileName = Component.translatable("text.snapper.generic") + " " + (this.index + 1);

            String creationString = "undefined";
            long creationTime = 0;
            try {
                creationTime = Files.readAttributes(icon.getPath(), BasicFileAttributes.class).creationTime().toMillis();
            } catch (IOException e) {
                minecraft.setScreen(new ScreenshotScreen(screenParent));
            }

            if (creationTime != -1L)
                creationString = DATE_FORMAT.format(Instant.ofEpochMilli(creationTime));

            context.blit(
                    RenderPipelines.GUI_TEXTURED,
                    GRID_SELECTION_BACKGROUND_TEXTURE,
                    getContentX(), getContentY(),
                    0, 0,
                    getContentWidth(), getContentHeight(),
                    16, 16
            );


            context.blitSprite(
                    RenderPipelines.GUI_TEXTURED,
                    clickThroughHovered && icon.loaded() ?
                            ScreenshotWidget.VIEW_HIGHLIGHTED_SPRITE : ScreenshotWidget.VIEW_SPRITE,
                    centreX - 16,
                    centreY - 16,
                    32,
                    32
            );

            context.drawString(
                    minecraft.font,
                    truncateFileName(fileName, getContentWidth(), 24),
                    getContentX() + 5,
                    getContentY() + 6,
                    CommonColors.WHITE,
                    true
            );

            context.drawString(
                    minecraft.font,
                    Component.translatable("text.snapper.created"),
                    getContentX() + 5,
                    getContentY() + getContentHeight() - 22,
                    CommonColors.LIGHT_GRAY,
                    true
            );

            context.drawString(
                    minecraft.font,
                    creationString,
                    getContentX() + 5,
                    getContentY() + getContentHeight() - 12,
                    CommonColors.LIGHT_GRAY,
                    true
            );
        }

        public String truncateFileName(String fileName, int maxWidth, int truncateLength) {
            String truncatedName = fileName;

            if (minecraft.font.width(truncatedName) > maxWidth) {
                truncatedName = truncatedName.substring(0, Math.min(fileName.length(), truncateLength)) + "...";
            }

            return truncatedName;
        }

        @Override
        public void setFocused(boolean focused) {
            if (focused) {
                setEntrySelected(this);
            }
            super.setFocused(focused);
        }

        @Override
        public @NonNull Component getNarration() {
            return Component.literal(this.iconFileName);
        }

        public boolean click() {
            if (this.icon == null) return false;
            playButtonClickSound(minecraft.getSoundManager());
            minecraft.setScreen(new ScreenshotViewerScreen(this.icon, icon.getPath(), this.screenParent, null));
            return true;
        }

        @Override
        public void close() {
            this.icon.close();
        }

        public long lastModified() {
            return lastModified.toMillis();
        }
    }
}
