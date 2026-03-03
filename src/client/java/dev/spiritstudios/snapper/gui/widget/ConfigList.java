package dev.spiritstudios.snapper.gui.widget;

import com.google.common.collect.ImmutableList;
import dev.spiritstudios.snapper.SnapperConfig;
import dev.spiritstudios.snapper.gui.screen.ConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class ConfigList extends ContainerObjectSelectionList<ConfigList.AbstractEntry> {
    private static final int BIG_BUTTON_WIDTH = 310;
    private static final int DEFAULT_ITEM_HEIGHT = 25;

    private final ConfigScreen screen;

    public ConfigList(Minecraft minecraft, int width, ConfigScreen screen) {
        super(minecraft, width, screen.layout.getContentHeight(), screen.layout.getHeaderHeight(), DEFAULT_ITEM_HEIGHT);

        this.centerListVertically = true;
        this.screen = screen;
    }

    public void addBig(AbstractWidget option) {
        this.addEntry(Entry.big(option, this.screen));
    }

    public void addSmall(AbstractWidget... options) {
        for (int i = 0; i < options.length; i += 2) {
            this.addSmall(options[i], i < options.length - 1 ? options[i + 1] : null);
        }
    }

    public void addSmall(AbstractWidget leftOption, @Nullable AbstractWidget rightOption) {
        this.addEntry(Entry.small(leftOption, rightOption, this.screen));
    }


    public void addHeader(Component text) {
        int lineHeight = 9;
        int paddingTop = this.children().isEmpty() ? 0 : lineHeight * 2;
        this.addEntry(new HeaderEntry(this.screen, text, paddingTop), paddingTop + lineHeight + 4);
    }

    @Override
    public int getRowWidth() {
        return BIG_BUTTON_WIDTH;
    }

    protected abstract static class AbstractEntry extends ContainerObjectSelectionList.Entry<ConfigList.AbstractEntry> {
    }

    public static class Entry extends AbstractEntry {
        private static final int X_OFFSET = 160;

        private final List<AbstractWidget> children;
        private final Screen screen;

        Entry(List<AbstractWidget> children, Screen screen) {
            this.children = List.copyOf(children);
            this.screen = screen;
        }

        public static Entry big(AbstractWidget option, Screen screen) {
            option.setWidth(310);
            return new Entry(List.of(option), screen);
        }

        public static Entry small(AbstractWidget leftOption, @Nullable AbstractWidget rightOption, Screen screen) {
            leftOption.setWidth(150);
            if (rightOption != null) rightOption.setWidth(150);

            return rightOption == null
                    ? new Entry(List.of(leftOption), screen)
                    : new Entry(List.of(leftOption, rightOption), screen);
        }

        @Override
        public void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, boolean isHovering, float partialTick) {
            int xOffset = 0;
            int x = this.screen.width / 2 - 155;

            for (AbstractWidget abstractWidget : this.children) {
                abstractWidget.setPosition(x + xOffset, this.getContentY());
                abstractWidget.render(guiGraphics, mouseX, mouseY, partialTick);
                xOffset += X_OFFSET;
            }
        }

        @Override
        public @NonNull List<? extends NarratableEntry> narratables() {
            return children;
        }

        @Override
        public @NonNull List<? extends GuiEventListener> children() {
            return children;
        }
    }

    protected static class HeaderEntry extends AbstractEntry {
        private final Screen screen;
        private final int paddingTop;
        private final StringWidget widget;

        protected HeaderEntry(final Screen screen, final Component text, final int paddingTop) {
            this.screen = screen;
            this.paddingTop = paddingTop;
            this.widget = new StringWidget(text, screen.getFont());
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return List.of(this.widget);
        }

        @Override
        public void renderContent(final GuiGraphics graphics, final int mouseX, final int mouseY, final boolean hovered, final float a) {
            this.widget.setPosition(this.screen.width / 2 - 155, this.getContentY() + this.paddingTop);
            this.widget.render(graphics, mouseX, mouseY, a);
        }

        public List<? extends GuiEventListener> children() {
            return List.of(this.widget);
        }
    }
}
