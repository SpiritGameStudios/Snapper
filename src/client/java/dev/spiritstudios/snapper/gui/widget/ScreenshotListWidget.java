package dev.spiritstudios.snapper.gui.widget;

import dev.spiritstudios.snapper.util.ScreenshotTexture;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.util.StringUtil;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;

public class ScreenshotListWidget extends ScreenshotsWidget {
    public ScreenshotListWidget(
            Minecraft client,
            int width, int height,
            int y,
            @Nullable ScreenshotsWidget previous,
            Screen parent
    ) {
        super(client, width, height, y, 36, previous, parent);
    }

    @Override
    public void repositionEntries() {
        super.repositionEntries();
        for (var entry : this.children()) {
            entry.setHeight(defaultEntryHeight);
        }
    }

    @Override
    protected void renderSelection(GuiGraphics context, Entry entry, int color) {
        // let elements handle it
    }

    @Override
    protected ScreenshotEntry createEntry(ScreenshotTexture icon) {
        return new ListScreenshotEntry(icon);
    }

    private class ListScreenshotEntry extends ScreenshotEntry {
        public ListScreenshotEntry(ScreenshotTexture icon) {
            super(icon);
        }

        @Override
        public void renderContent(GuiGraphics graphics, int mouseX, int mouseY, boolean isHovering, float partialTick) {
            String fileName = StringUtil.isNullOrEmpty(iconFileName) ?
                    Component.translatable("text.snapper.generic", this.index + 1).getString() :
                    iconFileName;

            graphics.drawString(
                    minecraft.font,
                    truncateFileName(fileName, getContentWidth() - 32 - 6, 29),
                    getContentX() + 32 + 3, getContentY() + 1,
                    CommonColors.WHITE,
                    false
            );

            String creationString = "undefined";

            Instant creationTime = Instant.EPOCH;

            try {
                creationTime = Files.readAttributes(icon.getPath(), BasicFileAttributes.class).creationTime().toInstant();
            } catch (IOException ignored) {
            }

            if (!creationTime.equals(Instant.EPOCH)) {
                creationString = Component.translatable("text.snapper.created").getString() + " " + DATE_FORMAT.format(creationTime);
            }

            graphics.drawString(
                    minecraft.font,
                    creationString,
                    getContentX() + 35, getContentY() + 12,
                    CommonColors.GRAY,
                    false
            );

            if (icon.loaded()) {
                graphics.blit(
                        RenderPipelines.GUI_TEXTURED,
                        this.icon.getTextureId(),
                        getContentX(), getContentY(),
                        (icon.getHeight()) / 3.0f + 32, 0,
                        getContentHeight(), getContentHeight(),
                        icon.getHeight(), icon.getHeight(),
                        icon.getWidth(), icon.getHeight()
                );
            }

            if (minecraft.options.touchscreen().get() || isHovering) {
                graphics.fill(getContentX(), getContentY(), getContentX() + 32, getContentY() + 32, 0xA0909090);
                graphics.blitSprite(
                        RenderPipelines.GUI_TEXTURED,
                        mouseX - getContentX() < 32 && this.icon.loaded() ?
                                ScreenshotsWidget.VIEW_HIGHLIGHTED_SPRITE :
                                ScreenshotsWidget.VIEW_SPRITE,
                        getContentX(), getContentY(),
                        32, 32
                );
            }
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
            setEntrySelected(this);

            boolean clickThrough = click.x() - ScreenshotListWidget.this.getRowLeft() <= 32.0F;

            if (!clickThrough && Util.getMillis() - this.time >= 250L) {
                this.time = Util.getMillis();
                return super.mouseClicked(click, doubled);
            }

            return click();
        }
    }
}
