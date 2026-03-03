package dev.spiritstudios.snapper.gui.widget;

import dev.spiritstudios.snapper.mixin.accessor.AbstractSliderButtonAccessor;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.DoubleFunction;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

public class ConfigSliderWidget<T> extends AbstractSliderButton {
    private final DoubleFunction<T> fromSliderValue;
    private final ToDoubleFunction<T> toSliderValue;

    private final Function<T, Component> messageSupplier;
    private final Function<T, @Nullable Tooltip> tooltipSupplier;
    private final Consumer<T> onValueChanged;

    private final Component title;

    private T convertedValue;

    public ConfigSliderWidget(
            int x, int y,
            int width, int height,
            Component message, T value,
            DoubleFunction<T> fromSliderValue, ToDoubleFunction<T> toSliderValue,
            Function<T, Component> messageSupplier,
            Function<T, @Nullable Tooltip> tooltipSupplier,
            Consumer<T> onValueChanged
    ) {
        super(x, y, width, height, message, toSliderValue.applyAsDouble(value));
        this.title = message;
        this.fromSliderValue = fromSliderValue;
        this.toSliderValue = toSliderValue;
        this.messageSupplier = messageSupplier;
        this.tooltipSupplier = tooltipSupplier;
        this.onValueChanged = onValueChanged;
        this.convertedValue = value;
        this.updateMessage();
    }

    @Override
    protected void updateMessage() {
        T value = fromSliderValue.apply(this.value);
        this.setMessage(CommonComponents.optionNameValue(title, messageSupplier.apply(value)));
        this.setTooltip(tooltipSupplier.apply(value));
    }

    @Override
    protected void applyValue() {
        onValueChanged.accept(fromSliderValue.apply(this.value));
    }

    @Override
    public void onRelease(MouseButtonEvent event) {
        super.onRelease(event);

        this.convertedValue = fromSliderValue.apply(value);

        if (this.value != toSliderValue.applyAsDouble(convertedValue)) {
            this.value = toSliderValue.applyAsDouble(convertedValue);
            this.updateMessage();
        }
    }
}
