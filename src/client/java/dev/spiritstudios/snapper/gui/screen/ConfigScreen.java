package dev.spiritstudios.snapper.gui.screen;

import dev.spiritstudios.snapper.SnapperConfig;
import dev.spiritstudios.snapper.gui.widget.ConfigList;
import dev.spiritstudios.snapper.gui.widget.ConfigSliderWidget;
import dev.spiritstudios.snapper.gui.widget.FolderSelectWidget;
import dev.spiritstudios.snapper.util.SnapperUtil;
import dev.spiritstudios.snapper.util.uploading.AxolotlClientApi;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class ConfigScreen extends Screen {
    private final @Nullable Screen lastScreen;

    public final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private ConfigList list;
    public final SnapperConfig.Mutable config = new SnapperConfig.Mutable();

    public ConfigScreen(@Nullable Screen lastScreen) {
        super(Component.translatable("config.snapper.title"));
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        super.init();

        this.layout.addTitleHeader(this.title, this.font);

        this.list = this.layout.addToContents(new ConfigList(this.minecraft, this.width, this));

        this.list.addHeader(Component.translatable("config.snapper.general"));

        this.list.addSmall(
                booleanButton(
                        "copyTakenScreenshot",
                        b -> config.copyTakenScreenshot = b,
                        config.copyTakenScreenshot
                ),
                enumButton(
                        "viewMode",
                        b -> config.viewMode = b,
                        config.viewMode,
                        ScreenshotScreen.ViewMode.class
                )
        );

        this.list.addBig(
                enumSlider(
                        "panoramaDimensions",
                        b -> config.panoramaDimensions = b,
                        config.panoramaDimensions,
                        SnapperUtil.PanoramaSize.class
                )
        );

        this.list.addHeader(Component.translatable("config.snapper.snapperButton"));

        this.list.addSmall(
                booleanButton(
                        "showOnTitleScreen",
                        b -> config.showOnTitleScreen = b,
                        config.showOnTitleScreen
                ),
                booleanButton(
                        "showInGameMenu",
                        b -> config.showInGameMenu = b,
                        config.showInGameMenu
                )
        );

        this.list.addHeader(Component.translatable("config.snapper.customScreenshotFolder"));


        var folderSelect = folderSelectWidget(
                "customScreenshotFolder",
                b -> config.path = b,
                config.path
        );

        folderSelect.setActive(config.enabled);

        this.list.addBig(booleanButton(
                "customScreenshotFolderEnabled",
                b -> {
                    folderSelect.setActive(b);
                    config.enabled = b;
                },
                config.enabled
        ));

        this.list.addBig(folderSelect);

        this.list.addHeader(Component.translatable("config.snapper.uploading"));

        this.list.addBig(enumButton(
                "termsAccepted",
                b -> config.termsAccepted = b,
                config.termsAccepted,
                AxolotlClientApi.TermsAcceptance.class
        ));

        this.layout.addToFooter(
                Button.builder(CommonComponents.GUI_DONE, button -> this.onClose())
                        .width(200)
                        .build()
        );

        this.layout.visitWidgets(this::addRenderableWidget);
        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
        if (this.list != null) {
            this.list.updateSize(this.width, this.layout);
        }
    }

    private @Nullable Tooltip getTooltip(String name) {
        Component tooltipText = Component.translatableWithFallback("config.snapper." + name + ".tooltip", "");
        return tooltipText.getString().isEmpty() ?
                null :
                Tooltip.create(tooltipText);
    }

    private AbstractWidget booleanButton(String name, Consumer<Boolean> setter, boolean currentValue) {
        Tooltip tooltip = getTooltip(name);

        return CycleButton.<Boolean>builder(
                        boolean_ -> boolean_
                                ? CommonComponents.OPTION_ON
                                : CommonComponents.OPTION_OFF
                )
                .withValues(List.of(Boolean.TRUE, Boolean.FALSE))
                .withTooltip(b -> tooltip)
                .withInitialValue(currentValue)
                .create(
                        0, 0,
                        150, 20,
                        Component.translatable("config.snapper." + name),
                        (cycleButton, object) -> {
                            setter.accept(object);
                        }
                );
    }

    private <T extends Enum<T>> AbstractWidget enumButton(
            String name,
            Consumer<T> setter,
            T currentValue,
            Class<T> clazz
    ) {
        Tooltip tooltip = getTooltip(name);

        return CycleButton.<T>builder(
                        t -> Component.translatable("config.snapper." + name + "." + t.toString().toLowerCase())
                )
                .withValues(Arrays.asList(clazz.getEnumConstants()))
                .withTooltip(b -> tooltip)
                .withInitialValue(currentValue)
                .create(
                        0, 0,
                        150, 20,
                        Component.translatable("config.snapper." + name),
                        (cycleButton, object) -> {
                            setter.accept(object);
                        }
                );
    }

    private <T extends Enum<T>> AbstractWidget enumSlider(
            String name,
            Consumer<T> setter,
            T currentValue,
            Class<T> clazz
    ) {
        Tooltip tooltip = getTooltip(name);
        List<T> values = Arrays.asList(clazz.getEnumConstants());

        return new ConfigSliderWidget<>(
                0, 0,
                150, 20,
                Component.translatable("config.snapper." + name),
                currentValue,
                slider -> {
                    if (slider >= 1.0) {
                        slider = 0.99999F;
                    }

                    int index = Mth.floor(Mth.map(slider, 0.0, 1.0, 0.0, values.size()));
                    return values.get(Mth.clamp(index, 0, values.size() - 1));
                },
                value -> {
                    if (value == values.getFirst()) {
                        return 0.0;
                    } else {
                        return value == values.getLast() ? 1.0 : Mth.map(values.indexOf(value), 0.0, values.size() - 1, 0.0, 1.0);
                    }
                },
                t -> Component.translatable("config.snapper." + name + "." + t.toString().toLowerCase()),
                t -> tooltip,
                setter
        );
    }

    private FolderSelectWidget folderSelectWidget(String name, Consumer<Path> setter, Path currentValue) {
        Tooltip tooltip = getTooltip(name);

        FolderSelectWidget widget = new FolderSelectWidget(
                0, 0,
                150, 20,
                new FolderSelectWidget.PathFunctions() {
                    private Path value = currentValue;

                    @Override
                    public Path get() {
                        return value;
                    }

                    @Override
                    public void set(Path path) {
                        value = path;
                        setter.accept(path);
                    }

                    @Override
                    public void reset() {
                        value = currentValue;
                    }
                },
                "config.snapper." + name + ".placeholder"
        );

        widget.setTooltip(tooltip);

        return widget;
    }

    @Override
    public void onClose() {
        minecraft.setScreen(lastScreen);
        config.saveAsync();
    }
}
