package dev.spiritstudios.snapper.gui.widget;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ElementListWidget;

public class OptionsScrollableWidget extends ElementListWidget<OptionsScrollableWidget.OptionEntry> {
    public OptionsScrollableWidget(MinecraftClient client, int width, int height, int y, int itemHeight) {
        super(client, width, height, y, itemHeight);
        this.centerListVertically = false;
    }

    @Override
    protected int getScrollbarX() {
        return super.getScrollbarX() + 32;
    }

    @Override
    public int getRowWidth() {
        return 400;
    }

    public void addOptions(List<ClickableWidget> options) {
        for (int i = 0; i < options.size(); i += 2) {
            ClickableWidget widget = options.get(i);
            ClickableWidget widget2 = i + 1 < options.size() ? options.get(i + 1) : null;

            this.addEntry(new OptionEntry(widget, widget2, this.width));
        }
    }

    protected static class OptionEntry extends Entry<OptionEntry> {
        private final List<ClickableWidget> widgets = new ArrayList<>();

        public OptionEntry(ClickableWidget widget, @Nullable ClickableWidget widget2, int width) {
            widget.setWidth(310);
            if (widget2 != null) {
                widget2.setWidth(150);
                widget.setWidth(150);
            }

            widget.setX(width / 2 - 155);
            if (widget2 != null) widget2.setX(width / 2 + 5);

            this.widgets.add(widget);
            if (widget2 != null) this.widgets.add(widget2);
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            widgets.forEach(widget -> {
                widget.setY(y);
                widget.render(context, mouseX, mouseY, tickDelta);
            });
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return widgets;
        }

        @Override
        public List<? extends Element> children() {
            return widgets;
        }
    }
}