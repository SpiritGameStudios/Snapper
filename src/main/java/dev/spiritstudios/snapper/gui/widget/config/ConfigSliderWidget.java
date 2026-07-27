package dev.spiritstudios.snapper.gui.widget.config;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class ConfigSliderWidget<T> extends AbstractSliderButton {
    protected final OptionInstance.SliderableValueSet<T> values;

    private final Function<T, Component> messageSupplier;
    private final Function<T, @Nullable Tooltip> tooltipSupplier;
    private final Consumer<T> onValueChanged;

    private final Component title;

    private T convertedValue;

    public ConfigSliderWidget(
            int x, int y,
            int width, int height,
            Component message, T value,
            OptionInstance.SliderableValueSet<T> values,
            Function<T, Component> messageSupplier,
            Function<T, @Nullable Tooltip> tooltipSupplier,
            Consumer<T> onValueChanged
    ) {
        super(x, y, width, height, message, values.toSliderValue(value));
        this.values = values;
        this.title = message;
        this.messageSupplier = messageSupplier;
        this.tooltipSupplier = tooltipSupplier;
        this.onValueChanged = onValueChanged;
        this.convertedValue = value;
        this.updateMessage();
    }

    @Override
    protected void updateMessage() {
        T value = values.fromSliderValue(this.value);
        this.setMessage(CommonComponents.optionNameValue(title, messageSupplier.apply(value)));
        this.setTooltip(tooltipSupplier.apply(value));
    }

    @Override
    protected void applyValue() {
        onValueChanged.accept(values.fromSliderValue(this.value));
    }

    @Override
    public boolean keyPressed(final KeyEvent event) {
        if (event.isSelection()) {
            this.canChangeValue = !this.canChangeValue;
            return true;
        }

        if (this.canChangeValue) {
            boolean left = event.isLeft();
            boolean right = event.isRight();
            if (left) {
                Optional<T> previous = values.previous(values.fromSliderValue(this.value));
                if (previous.isPresent()) {
                    this.setValue(values.toSliderValue(previous.get()));
                    return true;
                }
            }

            if (right) {
                Optional<T> next = values.next(values.fromSliderValue(this.value));
                if (next.isPresent()) {
                    this.setValue(values.toSliderValue(next.get()));
                    return true;
                }
            }

            if (left || right) {
                float direction = left ? -1.0F : 1.0F;
                this.setValue(this.value + direction / (this.width - 8));
                return true;
            }
        }

        return false;
    }

    @Override
    public void onRelease(MouseButtonEvent event) {
        super.onRelease(event);

        this.convertedValue = values.fromSliderValue(value);

        if (this.value != values.toSliderValue(convertedValue)) {
            this.value = values.toSliderValue(convertedValue);
            this.updateMessage();
        }
    }
}
