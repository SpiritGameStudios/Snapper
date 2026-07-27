package dev.spiritstudios.snapper.gui.widget.config;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

public class DynamicCappedSlider extends ConfigSliderWidget<Integer> {
    public DynamicCappedSlider(
            int x,
            int y,
            int width,
            int height,
            Component message,
            Integer value,
            OptionInstance.IntRangeBase values,
            Function<Integer, Component> messageSupplier,
            Function<Integer, @Nullable Tooltip> tooltipSupplier,
            Consumer<Integer> onValueChanged
    ) {
        super(x, y, width, height, message, value, values, messageSupplier, tooltipSupplier, onValueChanged);

        this.maximumSlider = values.toSliderValue(values.maxInclusive());
    }

    private double maximumSlider;

    public void setMaximum(int maximum) {
        this.maximumSlider = this.values.toSliderValue(maximum);
        this.setValue(value);
    }

    @Override
    protected void setValue(double newValue) {
        super.setValue(Math.min(newValue, maximumSlider));
    }
}
