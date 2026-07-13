package dev.spiritstudios.snapper.gui.widget;

import com.mojang.blaze3d.platform.InputConstants;
import dev.spiritstudios.snapper.Snapper;
import dev.spiritstudios.snapper.SnapperConfig;
import dev.spiritstudios.snapper.gui.screen.GalleryScreen;
import dev.spiritstudios.snapper.render.texture.GalleryTexture;
import dev.spiritstudios.snapper.util.SafeFiles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.LoadingDotsText;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.util.StringUtil;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.function.Supplier;

public abstract class GalleryWidget extends ObjectSelectionList<GalleryWidget.Entry> {
    protected static final Identifier VIEW_SPRITE = Snapper.id("screenshots/view");
    protected static final Identifier VIEW_HIGHLIGHTED_SPRITE = Snapper.id("screenshots/view_highlighted");
    protected static final Identifier GRID_SELECTION_BACKGROUND_TEXTURE = Snapper.id("textures/gui/grid_selection_background.png");

    protected final Supplier<List<GalleryTexture>> findScreenshots;

    protected final Screen screen;
    protected List<GalleryTexture> textures;

    @Override
    public void removeEntry(Entry entry) {
        super.removeEntry(entry);
    }

    public static GalleryWidget create(
            Minecraft minecraft,
            int width, int height,
            int y,
            Supplier<List<GalleryTexture>> findScreenshots,
            @Nullable GalleryWidget previous,
            Screen parent
    ) {
        if (SnapperConfig.HOLDER.get().viewMode() == GalleryScreen.ViewMode.GRID) {
            return new ScreenshotGridWidget(minecraft, width, height, y, findScreenshots, previous, parent);
        } else {
            return new ScreenshotListWidget(minecraft, width, height, y, findScreenshots, previous, parent);
        }
    }

    public GalleryWidget(
            Minecraft minecraft,
            int width, int height,
            int y, int itemHeight,
            Supplier<List<GalleryTexture>> findScreenshots,
            @Nullable GalleryWidget previous,
            Screen screen
    ) {
        super(minecraft, width, height, y, itemHeight);
        this.findScreenshots = findScreenshots;

        this.screen = screen;
        this.addEntry(new GalleryWidget.LoadingEntry(minecraft));

        if (previous == null) {
            this.textures = findScreenshots.get();
        } else {
            this.textures = previous.textures;
        }

        super.clearEntries();
        addEntries();
    }

    @Override
    protected void extractListBackground(final GuiGraphicsExtractor graphics) {
    }

    @Override
    protected void extractListSeparators(final GuiGraphicsExtractor graphics) {
    }

    @Override
    protected void extractListItems(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        for (Entry entry : this.children()) {
            if (entry.getY() + entry.getHeight() >= this.getY() && entry.getY() <= this.getBottom()) {
                this.extractItem(graphics, mouseX, mouseY, partialTick, entry);
            }
        }
    }

    @Override
    public void clearEntries() {
        for (GalleryTexture texture : this.textures) {
            texture.close();
        }

        this.textures = List.of();

        super.clearEntries();
    }

    protected void addEntries() {
        if (textures.isEmpty()) {
            addEntry(new EmptyEntry(minecraft));
        } else {
            for (GalleryTexture texture : textures) {
                addEntry(createEntry(texture));
            }
        }

        repositionEntries();
    }

    public synchronized void reload() {
        clearEntries();
        this.textures = findScreenshots.get();
        addEntries();
    }

    protected void setEntrySelected(@Nullable ScreenshotEntry entry) {
        super.setSelected(entry);
        if (this.screen instanceof GalleryScreen galleryScreen) {
            galleryScreen.setSelected(entry);
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

    protected abstract ScreenshotEntry createEntry(GalleryTexture texture);

    public abstract static class Entry extends ObjectSelectionList.Entry<Entry> implements AutoCloseable {
        public void close() {
        }
    }

    public static class LoadingEntry extends Entry implements AutoCloseable {
        private static final Component LOADING_LIST_TEXT = Component.translatable("text.snapper.loading");
        private final Minecraft minecraft;

        public LoadingEntry(Minecraft client) {
            this.minecraft = client;
        }

        @Override
        public @NonNull Component getNarration() {
            return LOADING_LIST_TEXT;
        }

        @Override
        public void extractContent(GuiGraphicsExtractor context, int mouseX, int mouseY, boolean isHovering, float partialTick) {
            Screen screen = this.minecraft.gui.screen();
            if (screen == null) throw new IllegalStateException();

            context.text(
                    this.minecraft.font,
                    LOADING_LIST_TEXT,
                    (screen.width - this.minecraft.font.width(LOADING_LIST_TEXT)) / 2,
                    getY() + (getHeight() - 9) / 2,
                    CommonColors.WHITE,
                    false
            );

            String loadString = LoadingDotsText.get(Util.getMillis());

            context.text(
                    this.minecraft.font,
                    loadString,
                    (screen.width - this.minecraft.font.width(loadString)) / 2,
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
        public void extractContent(GuiGraphicsExtractor context, int mouseX, int mouseY, boolean isHovering, float partialTick) {
            Screen screen = this.minecraft.gui.screen();
            if (screen == null) throw new IllegalStateException();

            context.text(
                    this.minecraft.font,
                    EMPTY_LIST_TEXT,
                    (screen.width - this.minecraft.font.width(EMPTY_LIST_TEXT)) / 2,
                    getY() + getHeight() / 2,
                    CommonColors.WHITE,
                    false
            );

            context.text(
                    this.minecraft.font,
                    EMPTY_CUSTOM_LIST_TEXT,
                    (screen.width - this.minecraft.font.width(EMPTY_CUSTOM_LIST_TEXT)) / 2,
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

        public final GalleryTexture texture;
        public final FileTime lastModified;

        protected final Component fileName;
        protected final Component creation;

        protected long time;
        protected boolean clickThroughHovered = false;
        protected final int index;

        public ScreenshotEntry(GalleryTexture texture) {
            this.texture = texture;

            this.index = children().indexOf(this);
            this.lastModified = SafeFiles.getLastModifiedTime(texture.path).orElse(FileTime.fromMillis(0L));

            String fileName = texture.path.getFileName().toString();

            this.fileName = StringUtil.isNullOrEmpty(fileName) ?
                    Component.translatable("text.snapper.generic", this.index + 1) :
                    Component.literal(fileName);

            Component creation = Component.translatable("text.snapper.unknown");

            long creationTime = 0;
            try {
                creationTime = Files.readAttributes(texture.path, BasicFileAttributes.class).creationTime().toMillis();
            } catch (IOException ignored) {
            }

            if (creationTime != -1L)
                creation = Component.literal(DATE_FORMAT.format(Instant.ofEpochMilli(creationTime)));

            this.creation = creation;
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
            return fileName;
        }

        public boolean click() {
            if (!texture.isLoaded()) return false;

            playButtonClickSound(minecraft.getSoundManager());
            minecraft.gui.setScreen(texture.createViewer(screen));
            return true;
        }
    }
}
