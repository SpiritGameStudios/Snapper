package dev.spiritstudios.snapper.gui.widget;

import com.mojang.blaze3d.platform.InputConstants;
import dev.spiritstudios.snapper.Snapper;
import dev.spiritstudios.snapper.SnapperConfig;
import dev.spiritstudios.snapper.gui.screen.ScreenshotListScreen;
import dev.spiritstudios.snapper.gui.screen.ScreenshotViewerScreen;
import dev.spiritstudios.snapper.util.SafeFiles;
import dev.spiritstudios.snapper.util.ScreenshotActions;
import dev.spiritstudios.snapper.util.ScreenshotTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
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
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;

public abstract class ScreenshotsWidget extends ObjectSelectionList<ScreenshotsWidget.Entry> {
    protected static final Identifier VIEW_SPRITE = Snapper.id("screenshots/view");
    protected static final Identifier VIEW_HIGHLIGHTED_SPRITE = Snapper.id("screenshots/view_highlighted");
    protected static final Identifier GRID_SELECTION_BACKGROUND_TEXTURE = Snapper.id("textures/gui/grid_selection_background.png");

    protected final Screen parent;
    protected List<ScreenshotTexture> textures;

    @Override
    public void removeEntry(Entry entry) {
        super.removeEntry(entry);
    }

    public static ScreenshotsWidget create(
            Minecraft client,
            int width, int height,
            int y, @Nullable ScreenshotsWidget previous,
            Screen parent
    ) {
        if (SnapperConfig.HOLDER.get().viewMode() == ScreenshotListScreen.ViewMode.GRID) {
            return new ScreenshotGridWidget(client, width, height, y, previous, parent);
        } else {
            return new ScreenshotListWidget(client, width, height, y, previous, parent);
        }
    }

    public ScreenshotsWidget(
            Minecraft minecraft,
            int width, int height,
            int y, int itemHeight,
            @Nullable ScreenshotsWidget previous,
            Screen parent
    ) {
        super(minecraft, width, height, y, itemHeight);

        this.parent = parent;
        this.addEntry(new ScreenshotsWidget.LoadingEntry(minecraft));

        if (previous == null) {
            this.textures = new ArrayList<>();

            for (Path screenshot : ScreenshotActions.getScreenshots()) {
                textures.add(ScreenshotTexture.createScreenshot(
                        this.minecraft.getTextureManager(),
                        screenshot
                ).orElseThrow());
            }
        } else {
            this.textures = previous.textures;
        }

        super.clearEntries();
        addEntries();
    }

    @Override
    public void clearEntries() {
        for (ScreenshotTexture texture : this.textures) {
            if (!texture.isClosed()) {
                texture.close();
            }
        }

        textures.clear();

        super.clearEntries();
    }

    protected void addEntries() {
        if (textures.isEmpty()) {
            addEntry(new EmptyEntry(minecraft));
        } else {
            for (ScreenshotTexture texture : textures) {
                addEntry(createEntry(texture));
            }
        }

        repositionEntries();
    }

    public void reload() {
        clearEntries();

        for (Path screenshot : ScreenshotActions.getScreenshots()) {
            textures.add(ScreenshotTexture.createScreenshot(
                    this.minecraft.getTextureManager(),
                    screenshot
            ).orElseThrow());
        }

        addEntries();
    }

    protected void setEntrySelected(@Nullable ScreenshotEntry entry) {
        super.setSelected(entry);
        if (this.parent instanceof ScreenshotListScreen screenshotScreen) {
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

    protected abstract ScreenshotEntry createEntry(ScreenshotTexture texture);

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

        public final ScreenshotTexture texture;
        public final FileTime lastModified;

        protected final Component fileName;
        protected final Component creation;

        protected long time;
        protected boolean clickThroughHovered = false;
        protected final int index;

        public ScreenshotEntry(ScreenshotTexture texture) {
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
            playButtonClickSound(minecraft.getSoundManager());
            minecraft.setScreen(new ScreenshotViewerScreen(texture, parent, null));
            return true;
        }
    }
}
