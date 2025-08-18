package dev.spiritstudios.snapper.gui.widget;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class SpecterButtonWidget extends ButtonWidget {
    protected final Supplier<Text> message;

    protected SpecterButtonWidget(int x, int y, int width, int height, Supplier<Text> message, PressAction onPress, NarrationSupplier narrationSupplier) {
        super(x, y, width, height, message.get(), onPress, narrationSupplier);

        this.message = message;
    }

    public static Builder builder(Supplier<Text> message, PressAction onPress) {
        return new Builder(message, onPress);
    }

    @Override
    public Text getMessage() {
        return message.get();
    }

    public static final class Builder {
        private final Supplier<Text> message;
        private final ButtonWidget.PressAction onPress;
        @Nullable
        private Tooltip tooltip;
        private int x;
        private int y;
        private int width = 150;
        private int height = 20;
        private ButtonWidget.NarrationSupplier narrationSupplier = ButtonWidget.DEFAULT_NARRATION_SUPPLIER;

        public Builder(Supplier<Text> message, ButtonWidget.PressAction onPress) {
            this.message = message;
            this.onPress = onPress;
        }

        public Builder position(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public Builder width(int width) {
            this.width = width;
            return this;
        }

        public Builder height(int height) {
            this.height = height;
            return this;
        }

        public Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder dimensions(int x, int y, int width, int height) {
            return this.position(x, y).size(width, height);
        }

        public Builder tooltip(@Nullable Tooltip tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        public Builder narrationSupplier(ButtonWidget.NarrationSupplier narrationSupplier) {
            this.narrationSupplier = narrationSupplier;
            return this;
        }

        public SpecterButtonWidget build() {
            SpecterButtonWidget buttonWidget = new SpecterButtonWidget(this.x, this.y, this.width, this.height, this.message, this.onPress, this.narrationSupplier);
            buttonWidget.setTooltip(this.tooltip);
            return buttonWidget;
        }
    }
}