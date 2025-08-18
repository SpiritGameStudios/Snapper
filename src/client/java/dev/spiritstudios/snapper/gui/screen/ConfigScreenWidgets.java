package dev.spiritstudios.snapper.gui.screen;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import dev.spiritstudios.snapper.gui.widget.SpecterButtonWidget;
import dev.spiritstudios.snapper.gui.widget.SpecterSliderWidget;
import dev.spiritstudios.snapper.util.PatternMap;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.polyfrost.oneconfig.api.config.v1.Property;


@SuppressWarnings("unchecked")
public final class ConfigScreenWidgets {
    private static final PatternMap<Function<Property<?>, ? extends ClickableWidget>> widgetFactories = new PatternMap<>();
    private static final Function<Property<?>, ? extends ClickableWidget> BOOLEAN_WIDGET_FACTORY = (configValue) -> {
        Property<Boolean> value = (Property<Boolean>) configValue;

        return SpecterButtonWidget.builder(
                () -> Text.translatable(value.getTitle()).append(": ").append(ScreenTexts.onOrOff(value.get())),
                button -> value.set(!value.get())
        ).build();
    };
    private static final Function<Property<?>, ? extends ClickableWidget> INTEGER_WIDGET_FACTORY = (configValue) -> {
        Property<Integer> value = (Property<Integer>) configValue;

        return SpecterSliderWidget.builder(value.get())
                .message((val) -> Text.translatable(value.getTitle()).append(String.format(": %.0f", val)))
                .range(value.getMetadata("min"), value.getMetadata("max"))
                .step((float) value.getMetadata("step") == 0 ? 1 : value.getMetadata("step"))
                .onValueChanged((val) -> value.set(val.intValue()))
                .build();
    };
    private static final Function<Property<?>, ? extends ClickableWidget> DOUBLE_WIDGET_FACTORY = (configValue) -> {
        Property<Double> value = (Property<Double>) configValue;

        return SpecterSliderWidget.builder(value.get())
                .message((val) -> Text.translatable(value.getTitle()).append(String.format(": %.2f", val)))
                .range(value.getMetadata("min"), value.getMetadata("max"))
                .step((float) value.getMetadata("step") == 0 ? 1 : value.getMetadata("step"))
                .onValueChanged(value::set)
                .build();
    };
    private static final Function<Property<?>, ? extends ClickableWidget> FLOAT_WIDGET_FACTORY = (configValue) -> {
        Property<Float> value = (Property<Float>) configValue;

        return SpecterSliderWidget.builder(value.get())
                .message((val) -> Text.translatable(configValue.getTitle()).append(String.format(": %.1f", val)))
                .range(value.getMetadata("min"), value.getMetadata("max"))
                .step((float) value.getMetadata("step") == 0 ? 1 : value.getMetadata("step"))
                .onValueChanged((val) -> value.set(val.floatValue()))
                .build();
    };
    private static final Function<Property<?>, ? extends ClickableWidget> STRING_WIDGET_FACTORY = (configValue) -> {
        Property<String> value = (Property<String>) configValue;

        TextFieldWidget widget = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, 0, 0, 0, 0, Text.of(value.get()));
        widget.setPlaceholder(Text.translatableWithFallback("%s.placeholder".formatted(configValue.getTitle()), "").formatted(Formatting.DARK_GRAY));

        widget.setText(value.get());
        widget.setChangedListener(value::set);
        widget.setSelectionEnd(0);
        widget.setSelectionStart(0);

        return widget;
    };
    private static final Function<Property<?>, ? extends ClickableWidget> ENUM_WIDGET_FACTORY = (configValue) -> {
        Property<Enum<?>> value = (Property<Enum<?>>) configValue;

        List<Enum<?>> enumValues = Arrays.stream(configValue.type.getEnumConstants())
                .filter(val -> val instanceof Enum<?>)
                .map(val -> (Enum<?>) val)
                .collect(Collectors.toList());

        if (enumValues.isEmpty()) throw new IllegalArgumentException("Enum values cannot be null");

        return SpecterButtonWidget.builder(
                () -> Text.translatable(configValue.getTitle()).append(": ").append(Text.translatable("%s.%s".formatted(configValue.getTitle(), value.get().toString().toLowerCase()))),
                button -> {
                    Enum<?> current = value.get();
                    int index = enumValues.indexOf(current);
                    value.set(enumValues.get((index + 1) % enumValues.size()));
                }
        ).build();
    };

    private ConfigScreenWidgets() {
    }

    public static void add(Class<?> clazz, Function<Property<?>, ? extends ClickableWidget> factory) {
        widgetFactories.put(clazz, factory);
    }

    @ApiStatus.Internal
    public static Function<Property<?>, ? extends ClickableWidget> getWidgetFactory(Property<?> value) {
        // We are using a switch instead of just adding to our map for 2 reasons:
        // 1. It's (usually) faster than a map lookup, as most of the time the value will be one of these types
        // 2. It lets us handle the lowercased names of primitive types, which are different Class<> instances because reasons
        return switch (value.get()) {
            case Boolean ignored -> BOOLEAN_WIDGET_FACTORY;
            case Integer ignored -> INTEGER_WIDGET_FACTORY;
            case Double ignored -> DOUBLE_WIDGET_FACTORY;
            case Float ignored -> FLOAT_WIDGET_FACTORY;
            case String ignored -> STRING_WIDGET_FACTORY;
            case Enum<?> ignored -> ENUM_WIDGET_FACTORY;
            case null, default -> widgetFactories.get(value.type);
        };
    }
}