package dev.spiritstudios.snapper.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.spiritstudios.snapper.Snapper;
import dev.spiritstudios.snapper.SnapperConfig;
import dev.spiritstudios.snapper.gui.screen.ScreenshotScreen;
import dev.spiritstudios.snapper.gui.screen.ScreenshotViewerScreen;
import dev.spiritstudios.snapper.mixin.accessor.EntryListWidgetAccessor;
import dev.spiritstudios.snapper.util.ScreenshotActions;
import dev.spiritstudios.snapper.util.ScreenshotImage;
import dev.spiritstudios.snapper.util.SnapperUtil;
import dev.spiritstudios.specter.api.core.exception.UnreachableException;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.navigation.NavigationDirection;
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
import net.minecraft.util.math.MathHelper;
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
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ScreenshotListWidget extends AlwaysSelectedEntryListWidget<ScreenshotListWidget.Entry> {
    private static final Identifier VIEW_TEXTURE = Snapper.id("screenshots/view");
    private static final Identifier VIEW_HIGHLIGHTED_TEXTURE = Snapper.id("screenshots/view_highlighted");

    private final Screen parent;

    public final CompletableFuture<List<ScreenshotEntry>> loadFuture;

    private final int gridItemHeight = 81;
    private final int listItemHeight = 36;
    private boolean showGrid = false;

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

        this.showGrid = SnapperConfig.INSTANCE.viewMode.get().equals(ScreenshotViewerScreen.ViewMode.GRID);

        ((EntryListWidgetAccessor) (Object) this).setItemHeight(this.showGrid ? this.gridItemHeight : this.listItemHeight);
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
                    .map(file -> new ScreenshotEntry(file, client, parent, screenshots))
                    .collect(Collectors.toList());
        });
    }

    private void setEntrySelected(@Nullable ScreenshotEntry entry) {
        super.setSelected(entry);
        if (this.parent instanceof ScreenshotScreen screenshotScreen)
            screenshotScreen.imageSelected(entry);
    }

    private int getColumnCount() {
        if (client.currentScreen != null) {
            int width = client.currentScreen.width;

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
        return showGrid ? 144 * getColumnCount() + 4 * (getColumnCount() - 1) : 220;
    }

    @Override
    protected void renderList(DrawContext context, int mouseX, int mouseY, float delta) {
        if (showGrid) {
            int rowLeft = this.getRowLeft();
            int rowWidth = this.getRowWidth();
            int entryHeight = this.itemHeight - 4;
            int gridItemWidth = 144;
            int entryWidth = showGrid ? gridItemWidth : rowWidth;
            int entryCount = this.getEntryCount();
            int spacing = showGrid ? (rowWidth - (getColumnCount() * entryWidth)) / (getColumnCount() - 1) : 0;

            for (int index = 0; index < entryCount; index++) {
                int rowTop = this.getRowTop(index);
                int rowBottom = this.getRowBottom(index);
                int colIndex = index % getColumnCount();
                int leftOffset = colIndex * (entryWidth + spacing);

                if (rowBottom >= this.getY() && rowTop <= this.getBottom()) {
                    this.renderEntry(context, mouseX, mouseY, delta, index, rowLeft + leftOffset, rowTop, entryWidth, entryHeight);
                }
            }
        } else {
            super.renderList(context, mouseX, mouseY, delta);
        }
    }

    @Override
    protected int getRowTop(int index) {
        return super.getRowTop(showGrid ? index / getColumnCount() : index);
    }

    @Override
    protected void drawSelectionHighlight(DrawContext context, int y, int entryWidth, int entryHeight, int borderColor, int fillColor) {
        // let elements handle it
    }

    @Override
    protected int getMaxPosition() {
        int totalRows = (int) (getEntryCount() / getColumnCount()) + (getEntryCount() % getColumnCount() > 0 ? 1 : 0);
        return showGrid ? totalRows * itemHeight : super.getMaxPosition();
    }

    public void toggleGrid() {
        this.showGrid = !this.showGrid;
        ((EntryListWidgetAccessor) (Object) this).setItemHeight(this.showGrid ? this.gridItemHeight : this.listItemHeight);
        for (var entry : this.children()) if (entry instanceof ScreenshotEntry sc) sc.setShowGrid(this.showGrid);

        SnapperConfig.INSTANCE.viewMode.set(this.showGrid ? ScreenshotViewerScreen.ViewMode.GRID : ScreenshotViewerScreen.ViewMode.LIST);
    }

    @Override
    protected @Nullable Entry getEntryAtPosition(double x, double y) {
        if (!showGrid) return super.getEntryAtPosition(x, y);

        int rowWidth = this.getRowWidth();
        int relX = MathHelper.floor(x - this.getRowLeft());
        int relY = MathHelper.floor(y - (double) this.getY()) - this.headerHeight;

        if (relX < 0 || relX > rowWidth || relY < 0 || relY > getBottom()) return null;

        int rowIndex = (relY + (int) this.getScrollAmount()) / this.itemHeight;
        int colIndex = MathHelper.floor(((float) relX / (float) rowWidth) * (float) getColumnCount());
        int entryIndex = rowIndex * getColumnCount() + colIndex;

        return entryIndex >= 0 && entryIndex < getEntryCount() ? getEntry(entryIndex) : null;
    }

    public abstract static class Entry extends AlwaysSelectedEntryListWidget.Entry<Entry> implements AutoCloseable {
        public void close() {
        }
    }

    @Override
    protected @Nullable Entry getNeighboringEntry(NavigationDirection direction, Predicate<Entry> predicate, @Nullable Entry selected) {
        if (!showGrid) return super.getNeighboringEntry(direction, predicate, selected);
        int offset = switch (direction) {
            case LEFT -> -1;
            case RIGHT -> 1;
            case UP -> -getColumnCount();
            case DOWN -> getColumnCount();
        };

        if (getEntryCount() > 0) {
            int entryIndex;
            if (selected == null) {
                entryIndex = offset > 0 ? 0 : getEntryCount() - 1;
            } else {
                entryIndex = this.children().indexOf(selected) + offset;
            }

            for (int k = entryIndex; k >= 0 && k < this.children().size(); k += offset) {
                Entry entry = getEntry(k);
                if (predicate.test(entry)) {
                    return entry;
                }
            }
        }

        return null;
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
        private boolean showGrid;
        private final List<File> screenshots;
        private boolean clickthroughHovered = false;

        public ScreenshotEntry(File screenshot, MinecraftClient client, Screen parent, List<File> screenshots) {
            this.showGrid = ScreenshotListWidget.this.showGrid;
            this.client = client;
            this.screenParent = parent;
            this.icon = ScreenshotImage.forScreenshot(this.client.getTextureManager(), screenshot.getName());
            this.iconPath = Path.of(screenshot.getPath());
            this.iconFileName = screenshot.getName();
            this.lastModified = screenshot.lastModified();
            this.screenshot = screenshot;
            this.screenshots = screenshots;
            this.loadIcon();
        }

        public void setShowGrid(boolean showGrid) {
            this.showGrid = showGrid;
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            if (this.showGrid) {
                renderGrid(context, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta);
                return;
            }
            renderList(context, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta);
        }

        public void renderList(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
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
                        entryHeight,
                        entryHeight,
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

        public void renderGrid(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int centreX = x + entryWidth / 2;
            int centreY = y + entryHeight / 2;

            clickthroughHovered = SnapperUtil.inBoundingBox(centreX - 16, centreY - 16, 32, 32, mouseX, mouseY);

            if (this.icon != null) {
                RenderSystem.enableBlend();
                context.drawTexture(
                        this.icon.getTextureId(),
                        x,
                        y,
                        entryWidth,
                        entryHeight,
                        0,
                        0,
                        icon.getWidth(),
                        icon.getHeight(),
                        icon.getWidth(),
                        icon.getHeight()
                );
                RenderSystem.disableBlend();
            }

            if (this.client.options.getTouchscreen().getValue() || (hovered && mouseX < x + entryWidth) || isSelectedEntry(index)) {
                renderMetadata(context, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta);
            }
        }

        public void renderMetadata(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            String fileName = this.iconFileName;

            int centreX = x + entryWidth / 2;
            int centreY = y + entryHeight / 2;

            if (StringHelper.isEmpty(fileName))
                fileName = Text.translatable("text.snapper.generic") + " " + (index + 1);

            String creationString = "undefined";
            long creationTime = 0;
            try {
                creationTime = Files.readAttributes(iconPath, BasicFileAttributes.class).creationTime().toMillis();
            } catch (IOException e) {
                client.setScreen(new ScreenshotScreen(screenParent));
            }

            if (creationTime != -1L)
                creationString = DATE_FORMAT.format(Instant.ofEpochMilli(creationTime));

            RenderSystem.enableBlend();
            Identifier hoverBackground = Identifier.of("snapper", "textures/gui/grid_selection_background.png");
            context.drawTexture(hoverBackground, x, y, 0, 0, entryWidth, entryHeight);
            RenderSystem.disableBlend();

            context.drawGuiTexture(
                    clickthroughHovered &&
                            this.icon != null ?
                            ScreenshotListWidget.VIEW_HIGHLIGHTED_TEXTURE : ScreenshotListWidget.VIEW_TEXTURE,
                    centreX - 16,
                    centreY - 16,
                    32,
                    32
            );

            context.drawText(
                    this.client.textRenderer,
                    fileName,
                    x + 5,
                    y + 6,
                    0xFFFFFF,
                    true
            );

            context.drawText(
                    this.client.textRenderer,
                    Text.translatable("text.snapper.created"),
                    x + 5,
                    y + entryHeight - 22,
                    Colors.LIGHT_GRAY,
                    true
            );

            context.drawText(
                    this.client.textRenderer,
                    creationString,
                    x + 5,
                    y + entryHeight - 12,
                    Colors.LIGHT_GRAY,
                    true
            );
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
        public void drawBorder(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            if (isSelectedEntry(index)) {
                context.fill(x - 2, y - 2, x + entryWidth + 2, y + entryHeight + 2, -1);
                context.fill(x - 1, y - 1, x + entryWidth + 1, y + entryHeight + 1, -16777216);
            }
        }

            @Override
        public void setFocused(boolean focused) {
            if (focused) {
                setEntrySelected(this);
            }
            super.setFocused(focused);
        }

        @Override
        public Text getNarration() {
            return Text.literal(this.iconFileName);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            ScreenshotListWidget.this.setEntrySelected(this);

            boolean clickThrough =
                    (
                            !this.showGrid &&
                                    mouseX - (double) ScreenshotListWidget.this.getRowLeft() <= 32.0
                    ) ||
                            (
                                    this.showGrid &&
                                            clickthroughHovered
                            );
            if (!clickThrough && Util.getMeasuringTimeMs() - this.time >= 250L) {
                this.time = Util.getMeasuringTimeMs();
                return super.mouseClicked(mouseX, mouseY, button);
            }

            return click();
        }

        public boolean click() {
            if (this.icon == null) return false;
            this.client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));

            this.client.setScreen(new ScreenshotViewerScreen(this.icon, this.screenshot, this.screenParent, this.screenshots));
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