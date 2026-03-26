package dev.spiritstudios.snapper.gui.widget;

import dev.spiritstudios.snapper.util.ScreenshotTexture;
import dev.spiritstudios.snapper.util.SnapperUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.screens.LoadingDotsText;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Util;
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

    public int getRows() {
        return (getItemCount() / getColumnCount()) + (getItemCount() % getColumnCount() > 0 ? 1 : 0);
    }

    @Override
    protected int contentHeight() {
        return this.getRows() * (this.defaultEntryHeight - 4) + 4;
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

            for (int i = entryIndex; i >= 0 && i < this.children().size(); i += offset) {
                Entry entry = this.children().get(i);
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
        int entryHeight = this.defaultEntryHeight - (2 * 2);
        int entryWidth = GRID_ENTRY_WIDTH;

        for (int index = 0; index < entryCount; index++) {
            int colIndex = index % getColumnCount();
            int leftOffset = colIndex * entryWidth;

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
    protected ScreenshotEntry createEntry(ScreenshotTexture texture) {
        return new GridScreenshotEntry(texture);
    }

    private class GridScreenshotEntry extends ScreenshotEntry {
        public GridScreenshotEntry(ScreenshotTexture texture) {
            super(texture);
        }

        private boolean safeIsSelected(Entry entry) {
            @Nullable Entry nullableSelected = getSelected();
            return (nullableSelected != null && nullableSelected.equals(entry));
        }

        @Override
        public void renderContent(GuiGraphics graphics, int mouseX, int mouseY, boolean isHovering, float partialTick) {
            texture.startLoading(minecraft);

            int centreX = getContentX() + getContentWidth() / 2;
            int centreY = getContentY() + getContentHeight() / 2;

            clickThroughHovered = SnapperUtil.inBoundingBox(centreX - 16, centreY - 16, 32, 32, mouseX, mouseY);

            if (this.texture.isLoaded()) {
                graphics.blit(
                        RenderPipelines.GUI_TEXTURED,
                        this.texture.textureLocation(),
                        getContentX(), getContentY(),
                        0, 0,
                        getContentWidth(), getContentHeight(),
                        texture.getWidth(), texture.getHeight(),
                        texture.getWidth(), texture.getHeight()
                );
            } else {
                String loadString = LoadingDotsText.get(Util.getMillis());

                graphics.drawString(
                        minecraft.font,
                        loadString,
                        getContentX() + (getContentWidth() - minecraft.font.width(loadString)) / 2, getContentY() + getContentHeight() / 2,
                        CommonColors.GRAY,
                        false
                );
            }

            if (minecraft.options.touchscreen().get() || (isHovering && mouseX < getX() + getWidth()) || safeIsSelected(this)) {
                graphics.blit(
                        RenderPipelines.GUI_TEXTURED,
                        GRID_SELECTION_BACKGROUND_TEXTURE,
                        getContentX(), getContentY(),
                        0, 0,
                        getContentWidth(), getContentHeight(),
                        16, 16
                );


                graphics.blitSprite(
                        RenderPipelines.GUI_TEXTURED,
                        clickThroughHovered && texture.isLoaded() ?
                                ScreenshotsWidget.VIEW_HIGHLIGHTED_SPRITE : ScreenshotsWidget.VIEW_SPRITE,
                        centreX - 16,
                        centreY - 16,
                        32, 32
                );

                graphics.drawString(
                        minecraft.font,
                        SnapperUtil.clipText(minecraft.font, fileName, getContentWidth() - 5),
                        getContentX() + 5,
                        getContentY() + 6,
                        CommonColors.WHITE,
                        true
                );

                graphics.drawString(
                        minecraft.font,
                        Component.translatable("text.snapper.created"),
                        getContentX() + 5,
                        getContentY() + getContentHeight() - 22,
                        CommonColors.LIGHT_GRAY,
                        true
                );

                graphics.drawString(
                        minecraft.font,
                        SnapperUtil.clipText(minecraft.font, creation, getContentWidth() - 5),
                        getContentX() + 5,
                        getContentY() + getContentHeight() - 12,
                        CommonColors.LIGHT_GRAY,
                        true
                );
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
