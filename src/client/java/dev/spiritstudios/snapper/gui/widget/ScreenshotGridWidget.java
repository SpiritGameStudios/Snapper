package dev.spiritstudios.snapper.gui.widget;

import dev.spiritstudios.snapper.util.ScreenshotTexture;
import dev.spiritstudios.snapper.util.SnapperUtil;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class ScreenshotGridWidget extends ScreenshotsWidget {
    public static final int GRID_ENTRY_WIDTH = 144;

    public ScreenshotGridWidget(
            Minecraft client,
            int width, int height,
            int y,
            @Nullable ScreenshotsWidget previous,
            Screen parent
    ) {
        super(client, width, height, y, 81, previous, parent);
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
        return GRID_ENTRY_WIDTH * getColumnCount() + 4 * (getColumnCount() - 1);
    }

    @Override
    public int getRowTop(int index) {
        return super.getRowTop(index / getColumnCount());
    }

    @Override
    public int maxScrollAmount() {
        int totalColumns = (getItemCount() / getColumnCount()) + (getItemCount() % getColumnCount() > 0 ? 1 : 0);
        return Math.max(0, totalColumns * defaultEntryHeight - defaultEntryHeight - this.height + 17);
    }

    @Override
    protected int contentHeight() {
        int totalRows = (getItemCount() / getColumnCount()) + (getItemCount() % getColumnCount() > 0 ? 1 : 0);
        return totalRows * this.defaultEntryHeight + 4;
    }

    @Override
    protected @Nullable Entry nextEntry(ScreenDirection direction, Predicate<Entry> predicate, @Nullable Entry selected) {
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

    @Override
    public void repositionEntries() {
        int entryCount = this.getItemCount();

        int rowTop = this.getY() + 2 - (int) this.scrollAmount();

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
    }

    @Override
    protected ScreenshotEntry createEntry(ScreenshotTexture icon) {
        return new GridScreenshotEntry(icon);
    }

    private class GridScreenshotEntry extends ScreenshotEntry {
        public GridScreenshotEntry(ScreenshotTexture icon) {
            super(icon);
        }

        private boolean safeIsSelected(Entry entry) {
            @Nullable Entry nullableSelected = getSelected();
            return (nullableSelected != null && nullableSelected.equals(entry));
        }

        @Override
        public void renderContent(GuiGraphics graphics, int mouseX, int mouseY, boolean isHovering, float partialTick) {
            int centreX = getContentX() + getContentWidth() / 2;
            int centreY = getContentY() + getContentHeight() / 2;

            clickThroughHovered = SnapperUtil.inBoundingBox(centreX - 16, centreY - 16, 32, 32, mouseX, mouseY);

            if (this.icon.loaded()) {
                graphics.blit(
                        RenderPipelines.GUI_TEXTURED,
                        this.icon.getTextureId(),
                        getContentX(), getContentY(),
                        0, 0,
                        getContentWidth(), getContentHeight(),
                        icon.getWidth(), icon.getHeight(),
                        icon.getWidth(), icon.getHeight()
                );
            }

            if (minecraft.options.touchscreen().get() || (isHovering && mouseX < getX() + getWidth()) || safeIsSelected(this)) {
                renderMetadata(graphics, mouseX, mouseY, isHovering, partialTick);
            }
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
            setEntrySelected(this);

            if (!clickThroughHovered && Util.getMillis() - this.time >= 250L) {
                this.time = Util.getMillis();
                return super.mouseClicked(click, doubled);
            }

            return click();
        }
    }
}
