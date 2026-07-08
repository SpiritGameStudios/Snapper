package dev.spiritstudios.snapper.gui.widget;

import dev.spiritstudios.snapper.util.ScreenshotTexture;
import dev.spiritstudios.snapper.util.SnapperUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.LoadingDotsText;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

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
    protected ScreenshotEntry createEntry(ScreenshotTexture texture) {
        return new ListScreenshotEntry(texture);
    }

    private class ListScreenshotEntry extends ScreenshotEntry {
        public ListScreenshotEntry(ScreenshotTexture texture) {
            super(texture);
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean isHovering, float partialTick) {
            texture.startLoading(minecraft, false);

            graphics.text(
                    minecraft.font,
                    SnapperUtil.clipText(minecraft.font, fileName, getContentWidth() - 32 - 6),
                    getContentX() + 32 + 3, getContentY() + 1,
                    CommonColors.WHITE,
                    false
            );

            graphics.text(
                    minecraft.font,
                    Component.translatable("text.snapper.created")
                            .append(CommonComponents.space())
                            .append(creation),
                    getContentX() + 35, getContentY() + 12,
                    CommonColors.GRAY,
                    false
            );

            if (texture.isLoaded()) {
                graphics.blit(
                        RenderPipelines.GUI_TEXTURED,
                        this.texture.textureLocation(),
                        getContentX(), getContentY(),
                        (texture.getHeight()) / 3.0f + 32, 0,
                        getContentHeight(), getContentHeight(),
                        texture.getHeight(), texture.getHeight(),
                        texture.getWidth(), texture.getHeight()
                );
            } else {
                String loadString = LoadingDotsText.get(Util.getMillis());

                graphics.text(
                        minecraft.font,
                        loadString,
                        getContentX() + (16 - minecraft.font.width(loadString) / 2),
                        (getContentY() + getContentHeight() / 2) - minecraft.font.lineHeight / 2,
                        CommonColors.GRAY,
                        false
                );
            }

            if (isHovering) {
                graphics.fill(getContentX(), getContentY(), getContentX() + 32, getContentY() + 32, 0xA0909090);
                graphics.blitSprite(
                        RenderPipelines.GUI_TEXTURED,
                        mouseX - getContentX() < 32 && this.texture.isLoaded() ?
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
