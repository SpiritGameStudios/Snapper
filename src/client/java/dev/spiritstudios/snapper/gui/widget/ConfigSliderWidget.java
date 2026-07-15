package dev.spiritstudios.snapper.gui.widget;

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
    private final List<T> values;

    private final Function<T, Component> messageSupplier;
    private final Function<T, @Nullable Tooltip> tooltipSupplier;
    private final Consumer<T> onValueChanged;

    private final Component title;

    private T convertedValue;

    public ConfigSliderWidget(
            int x, int y,
            int width, int height,
            Component message, T value,
            List<T> values,
            Function<T, Component> messageSupplier,
            Function<T, @Nullable Tooltip> tooltipSupplier,
            Consumer<T> onValueChanged
    ) {
        super(x, y, width, height, message, 0.0);
        this.values = values;
        this.value = toSliderValue(value);
        this.title = message;
        this.messageSupplier = messageSupplier;
        this.tooltipSupplier = tooltipSupplier;
        this.onValueChanged = onValueChanged;
        this.convertedValue = value;
        this.updateMessage();
    }

    protected T fromSliderValue(double slider) {
        if (slider >= 1.0) {
            slider = 0.99999F;
        }

        int index = Mth.floor(Mth.map(slider, 0.0, 1.0, 0.0, values.size()));
        return values.get(Mth.clamp(index, 0, values.size() - 1));
    }

    protected double toSliderValue(T value) {
        if (Objects.equals(value, values.getFirst())) {
            return 0.0;
        } else {
            return Objects.equals(value, values.getLast()) ? 1.0 : Mth.map(values.indexOf(value), 0.0, values.size() - 1, 0.0, 1.0);
        }
    }


    public Optional<T> next(final T current) {
        int currentIndex = this.values.indexOf(current);
        int nextIndex = Mth.clamp(currentIndex + 1, 0, this.values.size() - 1);
        return Optional.of(this.values.get(nextIndex));
    }

    public Optional<T> previous(final T current) {
        int currentIndex = this.values.indexOf(current);
        int previousIndex = Mth.clamp(currentIndex - 1, 0, this.values.size() - 1);
        return Optional.of(this.values.get(previousIndex));
    }


    @Override
    protected void updateMessage() {
        T value = fromSliderValue(this.value);
        this.setMessage(CommonComponents.optionNameValue(title, messageSupplier.apply(value)));
        this.setTooltip(tooltipSupplier.apply(value));
    }

    @Override
    protected void applyValue() {
        onValueChanged.accept(fromSliderValue(this.value));
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
                Optional<T> previous = previous(fromSliderValue(this.value));
                if (previous.isPresent()) {
                    this.setValue(toSliderValue(previous.get()));
                    return true;
                }
            }

            if (right) {
                Optional<T> next = next(fromSliderValue(this.value));
                if (next.isPresent()) {
                    this.setValue(toSliderValue(next.get()));
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

        this.convertedValue = fromSliderValue(value);

        if (this.value != toSliderValue(convertedValue)) {
            this.value = toSliderValue(convertedValue);
            this.updateMessage();
        }
    }
}
