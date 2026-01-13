package dev.spiritstudios.snapper.gui.widget;

import com.mojang.blaze3d.platform.InputConstants;
import dev.spiritstudios.snapper.Snapper;
import dev.spiritstudios.snapper.SnapperConfig;
import dev.spiritstudios.snapper.gui.screen.ScreenshotScreen;
import dev.spiritstudios.snapper.gui.screen.ScreenshotViewerScreen;
import dev.spiritstudios.snapper.mixin.accessor.AbstractSelectionListAccessor;
import dev.spiritstudios.snapper.util.ScreenshotTexture;
import dev.spiritstudios.snapper.util.SafeFiles;
import dev.spiritstudios.snapper.util.ScreenshotActions;
import dev.spiritstudios.snapper.util.SnapperUtil;
import dev.spiritstudios.specter.api.core.exception.UnreachableException;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.screens.LoadingDotsText;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import org.jetbrains.annotations.Nullable;

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
import java.util.function.Predicate;

public class ScreenshotListWidget extends ObjectSelectionList<ScreenshotListWidget.Entry> {
    private static final ResourceLocation VIEW_SPRITE = Snapper.id("screenshots/view");
    private static final ResourceLocation VIEW_HIGHLIGHTED_SPRITE = Snapper.id("screenshots/view_highlighted");

    private static final ResourceLocation GRID_SELECTION_BACKGROUND_TEXTURE = Snapper.id("textures/gui/grid_selection_background.png");

    private final Screen parent;

    public final CompletableFuture<List<ScreenshotEntry>> loadFuture;

    public static final int GRID_ENTRY_WIDTH = 144;

    private final int gridItemHeight = 81;
    private final int listItemHeight = 36;
    private boolean showGrid = false;

    public ScreenshotListWidget(
            Minecraft client,
            int width, int height,
            int y, int itemHeight,
            @Nullable ScreenshotListWidget previous,
            Screen parent
    ) {
        super(client, width, height, y, itemHeight);

        this.parent = parent;
        this.addEntry(new LoadingEntry(client));

        this.loadFuture = previous != null ? previous.loadFuture : load(client);

        this.loadFuture.thenAccept(entries -> {
            this.clearEntries();
            entries.forEach(this::addEntry);

            if (entries.isEmpty()) {
                this.addEntry(new EmptyEntry(client));
            }

            repositionEntries();
        });

        this.showGrid = SnapperConfig.HOLDER.get().viewMode().equals(ScreenshotScreen.ViewMode.GRID);

        ((AbstractSelectionListAccessor) this).setDefaultEntryHeight(this.showGrid ? this.gridItemHeight : this.listItemHeight);
        repositionEntries();
    }

    @Override
    protected void clearEntries() {
        this.children().forEach(Entry::close);
        super.clearEntries();
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        if (input.key() == InputConstants.KEY_RETURN) {
            Entry entry = this.getSelected();
            if (entry instanceof ScreenshotEntry screenshotEntry) return screenshotEntry.click();
        }
        return super.keyPressed(input);
    }

    public CompletableFuture<List<ScreenshotEntry>> load(Minecraft client) {
        return CompletableFuture.supplyAsync(() -> {
                    List<Path> screenshots = ScreenshotActions.getScreenshots();

                    return screenshots.parallelStream()
                            .flatMap(path -> ScreenshotTexture.createScreenshot(client.getTextureManager(), path).stream())
                            .peek(screenshotImage -> screenshotImage.load()
                                    .exceptionally(throwable -> {
                                        Snapper.LOGGER.error("An error occurred while loading the screenshot list", throwable);
                                        return null;
                                    }))
                            .map(image -> new ScreenshotEntry(image, client, parent, screenshots))
                            .sorted(Comparator.comparingLong(ScreenshotEntry::lastModified).reversed())
                            .toList();
                })
                .exceptionally(throwable -> {
                    Snapper.LOGGER.error("An error occurred while loading the screenshot list", throwable);
                    return Collections.emptyList();
                });
    }

    private void setEntrySelected(@Nullable ScreenshotEntry entry) {
        super.setSelected(entry);
        if (this.parent instanceof ScreenshotScreen screenshotScreen) {
            screenshotScreen.imageSelected(entry);
        }
    }

    private int getColumnCount() {
        if (minecraft.screen != null) {
            int width = minecraft.screen.width;

            if (width < 480) {
                return 2;
            }
            if (width < 720) {
                return 3;
            }
            return 4;
        }
        return 3;
    }

    @Override
    public int getRowWidth() {
        return showGrid ? GRID_ENTRY_WIDTH * getColumnCount() + 4 * (getColumnCount() - 1) : 220;
    }

    @Override
    public void repositionEntries() {
        int entryCount = this.getItemCount();
        if (showGrid) {
            int rowTop = this.getY() + 2 - (int)this.scrollAmount();

            int rowLeft = this.getRowLeft();
            int rowWidth = this.getRowWidth();
            int entryHeight = this.defaultEntryHeight - 4;
            int entryWidth = GRID_ENTRY_WIDTH;
            int spacing = (rowWidth - (getColumnCount() * entryWidth)) / (getColumnCount() - 1);

            for (int index = 0; index < entryCount; index++) {
                int colIndex = index % getColumnCount();
                int leftOffset = colIndex * (entryWidth + spacing);

                ScreenshotListWidget.Entry entry = this.children().get(index);
                entry.setY(rowTop);
                entry.setX(rowLeft + leftOffset);
                entry.setWidth(entryWidth);
                entry.setHeight(entryHeight);

                if (colIndex == getColumnCount() - 1) {
                    rowTop += entry.getHeight();
                }
            }
        } else {
            super.repositionEntries();
            for (var entry : this.children()) {
                entry.setHeight(defaultEntryHeight);
            }
        }
    }

    @Override
    public int getRowTop(int index) {
        return super.getRowTop(showGrid ? index / getColumnCount() : index);
    }

    @Override
    protected void renderSelection(GuiGraphics context, Entry entry, int color) {
        // let elements handle it
    }

    @Override
    public int maxScrollAmount() {
        int totalColumns = (getItemCount() / getColumnCount()) + (getItemCount() % getColumnCount() > 0 ? 1 : 0);
        return showGrid ? Math.max(0, totalColumns * defaultEntryHeight - this.height) : super.maxScrollAmount();
    }

    @Override
    protected int contentHeight() {
        if (!this.showGrid) return super.contentHeight();
        int totalRows = (getItemCount() / getColumnCount()) + (getItemCount() % getColumnCount() > 0 ? 1 : 0);
        return totalRows * this.defaultEntryHeight + 4;
    }

    public void toggleGrid() {
        this.showGrid = !this.showGrid;
        ((AbstractSelectionListAccessor) this).setDefaultEntryHeight(this.showGrid ? this.gridItemHeight : this.listItemHeight);
        for (var entry : this.children()) if (entry instanceof ScreenshotEntry sc) sc.setShowGrid(this.showGrid);

        SnapperConfig.Mutable mutable = SnapperConfig.mutable();
        mutable.viewMode = this.showGrid ? ScreenshotScreen.ViewMode.GRID : ScreenshotScreen.ViewMode.LIST;
        mutable.save();

        repositionEntries();
    }

    @Override
    protected @Nullable Entry getEntryAtPosition(double x, double y) {
        if (!showGrid) return super.getEntryAtPosition(x, y);

        int rowWidth = this.getRowWidth();
        int relX = Mth.floor(x - this.getRowLeft());
        int relY = Mth.floor(y - (double) this.getY());

        if (relX < 0 || relX > rowWidth || relY < 0 || relY > getBottom()) return null;

        int rowIndex = (relY + (int) this.scrollAmount()) / this.defaultEntryHeight;
        int colIndex = Mth.floor(((float) relX / (float) rowWidth) * (float) getColumnCount());
        int entryIndex = rowIndex * getColumnCount() + colIndex;

        return entryIndex >= 0 && entryIndex < getItemCount() ? this.children().get(entryIndex) : null;
    }

    public abstract static class Entry extends ObjectSelectionList.Entry<Entry> implements AutoCloseable {
        public void close() {
        }
    }

    @Override
    protected @Nullable Entry nextEntry(ScreenDirection direction, Predicate<Entry> predicate, @Nullable Entry selected) {
        if (!showGrid) return super.nextEntry(direction, predicate, selected);
        int offset = switch (direction) {
            case LEFT -> -1;
            case RIGHT -> 1;
            case UP -> -getColumnCount();
            case DOWN -> getColumnCount();
        };

        if (getItemCount() > 0) {
            int entryIndex;
            if (selected == null) {
                entryIndex = offset > 0 ? 0 : getItemCount() - 1;
            } else {
                entryIndex = this.children().indexOf(selected) + offset;
            }

            for (int k = entryIndex; k >= 0 && k < this.children().size(); k += offset) {
                Entry entry = this.children().get(k);
                if (predicate.test(entry)) {
                    return entry;
                }
            }
        }

        return null;
    }

    public static class LoadingEntry extends Entry implements AutoCloseable {
        private static final Component LOADING_LIST_TEXT = Component.translatable("text.snapper.loading");
        private final Minecraft client;

        public LoadingEntry(Minecraft client) {
            this.client = client;
        }

        @Override
        public Component getNarration() {
            return LOADING_LIST_TEXT;
        }

        @Override
        public void renderContent(GuiGraphics context, int mouseX, int mouseY, boolean isHovering, float partialTick) {
            if (this.client.screen == null) throw new UnreachableException();

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
        public Component getNarration() {
            return EMPTY_LIST_TEXT;
        }

        @Override
        public void renderContent(GuiGraphics context, int mouseX, int mouseY, boolean isHovering, float partialTick) {
            if (this.minecraft.screen == null) throw new UnreachableException();

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

    public class ScreenshotEntry extends Entry implements AutoCloseable {
        public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter
                .ofLocalizedDateTime(FormatStyle.SHORT)
                .withZone(ZoneId.systemDefault());

        public final FileTime lastModified;
        private final Minecraft client;
        public final ScreenshotTexture icon;
        public final String iconFileName;
        public final Screen screenParent;
        private long time;
        private boolean showGrid;
        private final List<Path> screenshots;
        private boolean clickThroughHovered = false;
        private final int index;

        public ScreenshotEntry(ScreenshotTexture icon, Minecraft client, Screen parent, List<Path> screenshots) {
            this.showGrid = ScreenshotListWidget.this.showGrid;
            this.client = client;
            this.screenParent = parent;
            this.icon = icon;
            this.iconFileName = icon.getPath().getFileName().toString();
            this.lastModified = SafeFiles.getLastModifiedTime(icon.getPath()).orElse(FileTime.fromMillis(0L));
            this.screenshots = screenshots;
            this.index = children().indexOf(this);
        }

        public void setShowGrid(boolean showGrid) {
            this.showGrid = showGrid;
        }

        private boolean safeIsSelected(Entry entry) {
            @Nullable Entry nullableSelected = getSelected();
            return (nullableSelected != null && nullableSelected.equals(entry));
        }

        @Override
        public void renderContent(GuiGraphics context, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            if (this.showGrid) {
                renderGrid(context, mouseX, mouseY, hovered, tickDelta);
                return;
            }
            renderList(context, mouseX, mouseY, hovered, tickDelta);
        }

        public void renderList(GuiGraphics context, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            String fileName = this.iconFileName;
            String creationString = "undefined";

            long creationTime = 0;
            try {
                creationTime = Files.readAttributes(icon.getPath(), BasicFileAttributes.class).creationTime().toMillis();
            } catch (IOException e) {
                client.setScreen(new ScreenshotScreen(screenParent));
            }

            if (creationTime != -1L)
                creationString = Component.translatable("text.snapper.created").getString() + " " + DATE_FORMAT.format(Instant.ofEpochMilli(creationTime));

            if (StringUtil.isNullOrEmpty(fileName))
                fileName = Component.translatable("text.snapper.generic") + " " + (this.index + 1);

            context.drawString(
                    this.client.font,
                    truncateFileName(fileName, getContentWidth() - 32 - 6, 29),
                    getContentX() + 32 + 3, getContentY() + 1,
                    CommonColors.WHITE,
                    false
            );

            context.drawString(
                    this.client.font,
                    creationString,
                    getContentX() + 35, getContentY() + 12,
                    CommonColors.GRAY,
                    false
            );

            if (icon.loaded()) {
                context.blit(
                        RenderPipelines.GUI_TEXTURED,
                        this.icon.getTextureId(),
                        getContentX(), getContentY(),
                        (icon.getHeight()) / 3.0f + 32, 0,
                        getContentHeight(), getContentHeight(),
                        icon.getHeight(), icon.getHeight(),
                        icon.getWidth(), icon.getHeight()
                );
            }

            if (this.client.options.touchscreen().get() || hovered) {
                context.fill(getContentX(), getContentY(), getContentX() + 32, getContentY() + 32, 0xA0909090);
                context.blitSprite(
                        RenderPipelines.GUI_TEXTURED,
                        mouseX - getContentX() < 32 && this.icon.loaded() ?
                                ScreenshotListWidget.VIEW_HIGHLIGHTED_SPRITE :
                                ScreenshotListWidget.VIEW_SPRITE,
                        getContentX(), getContentY(),
                        32, 32
                );
            }
        }

        public void renderGrid(GuiGraphics context, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int centreX = getContentX() + getContentWidth() / 2;
            int centreY = getContentY() + getContentHeight() / 2;

            clickThroughHovered = SnapperUtil.inBoundingBox(centreX - 16, centreY - 16, 32, 32, mouseX, mouseY);

            if (this.icon.loaded()) {
                context.blit(
                        RenderPipelines.GUI_TEXTURED,
                        this.icon.getTextureId(),
                        getContentX(), getContentY(),
                        0, 0,
                        getContentWidth(), getContentHeight(),
                        icon.getWidth(), icon.getHeight(),
                        icon.getWidth(), icon.getHeight()
                );
            }

            if (this.client.options.touchscreen().get() || (hovered && mouseX < getX() + getWidth()) || safeIsSelected(this)) {
                renderMetadata(context, mouseX, mouseY, hovered, tickDelta);
            }
        }

        public void renderMetadata(GuiGraphics context, int mouseX, int mouseY, boolean hovered, float deltaTick) {
            String fileName = this.iconFileName;

            int centreX = getContentX() + getContentWidth() / 2;
            int centreY = getContentY() + getContentHeight() / 2;

            if (StringUtil.isNullOrEmpty(fileName))
                fileName = Component.translatable("text.snapper.generic") + " " + (this.index + 1);

            String creationString = "undefined";
            long creationTime = 0;
            try {
                creationTime = Files.readAttributes(icon.getPath(), BasicFileAttributes.class).creationTime().toMillis();
            } catch (IOException e) {
                client.setScreen(new ScreenshotScreen(screenParent));
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
                            ScreenshotListWidget.VIEW_HIGHLIGHTED_SPRITE : ScreenshotListWidget.VIEW_SPRITE,
                    centreX - 16,
                    centreY - 16,
                    32,
                    32
            );

            context.drawString(
                    this.client.font,
                    truncateFileName(fileName, getContentWidth(), 24),
                    getContentX() + 5,
                    getContentY() + 6,
                    CommonColors.WHITE,
                    true
            );

            context.drawString(
                    this.client.font,
                    Component.translatable("text.snapper.created"),
                    getContentX() + 5,
                    getContentY() + getContentHeight() - 22,
                    CommonColors.LIGHT_GRAY,
                    true
            );

            context.drawString(
                    this.client.font,
                    creationString,
                    getContentX() + 5,
                    getContentY() + getContentHeight() - 12,
                    CommonColors.LIGHT_GRAY,
                    true
            );
        }

        public String truncateFileName(String fileName, int maxWidth, int truncateLength) {
            String truncatedName = fileName;
            if (this.client.font.width(truncatedName) > maxWidth)
                truncatedName = truncatedName.substring(0, Math.min(fileName.length(), truncateLength)) + "...";
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
        public Component getNarration() {
            return Component.literal(this.iconFileName);
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
            ScreenshotListWidget.this.setEntrySelected(this);

            boolean clickThrough =
                    (
                            !this.showGrid &&
                                    click.x() - (double) ScreenshotListWidget.this.getRowLeft() <= 32.0
                    ) ||
                            (
                                    this.showGrid &&
                                            clickThroughHovered
                            );
            if (!clickThrough && Util.getMillis() - this.time >= 250L) {
                this.time = Util.getMillis();
                return super.mouseClicked(click, doubled);
            }

            return click();
        }

        public boolean click() {
            if (this.icon == null) return false;
            playButtonClickSound(this.client.getSoundManager());
            this.client.setScreen(new ScreenshotViewerScreen(this.icon, icon.getPath(), this.screenParent, this.screenshots));
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
