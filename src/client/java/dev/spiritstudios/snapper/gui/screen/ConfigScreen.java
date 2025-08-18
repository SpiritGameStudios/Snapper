package dev.spiritstudios.snapper.gui.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import dev.spiritstudios.snapper.Snapper;
import dev.spiritstudios.snapper.gui.widget.OptionsScrollableWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.polyfrost.oneconfig.api.config.v1.Config;
import org.polyfrost.oneconfig.api.config.v1.Property;

public class ConfigScreen extends Screen {
    protected final Config config;
    protected final Screen parent;

    public ConfigScreen(Config config, Screen parent) {
        super(Text.translatable("config.snapper.title"));
        this.config = config;
        this.parent = parent;
    }


    @Override
    protected void init() {
        super.init();
        Objects.requireNonNull(this.client);

        OptionsScrollableWidget scrollableWidget = new OptionsScrollableWidget(this.client, this.width, this.height - 64, 32, 25);

        List<ClickableWidget> options = new ArrayList<>();
        config.getTree().map.forEach((key, value) -> {
            if (value instanceof Property<?>) {
                Property<?> property = (Property<?>) value;
                Function<Property<?>, ? extends ClickableWidget> factory = ConfigScreenWidgets.getWidgetFactory(property);
                if (factory == null) {
                    Snapper.LOGGER.warn("No widget factory found for {}", property.type.getSimpleName());
                    return;
                }

                ClickableWidget widget = factory.apply(property);
                if (widget == null)
                    throw new IllegalStateException("Widget factory returned null for %s".formatted(property.type.getSimpleName()));

                widget.setWidth(0);
                widget.setHeight(20);

                Text tooltip = Text.translatableWithFallback("%s.tooltip".formatted(property.getTitle()), "");
                if (!tooltip.getString().isEmpty()) widget.setTooltip(Tooltip.of(tooltip));

                options.add(widget);
            }
        });

        scrollableWidget.addOptions(options);
        this.addDrawableChild(scrollableWidget);
        this.addDrawableChild(new ButtonWidget.Builder(ScreenTexts.DONE, button -> close()).dimensions(this.width / 2 - 100, this.height - 27, 200, 20).build());
    }

    @Override
    public void close() {
        save();

        Objects.requireNonNull(this.client);
        this.client.setScreen(this.parent);
    }

    public void save() {
        config.save();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);

        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
    }
}