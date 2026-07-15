package dev.spiritstudios.snapper.gui.screen;

import dev.spiritstudios.snapper.SnapperConfig;
import dev.spiritstudios.snapper.gui.widget.ConfigList;
import dev.spiritstudios.snapper.gui.widget.ConfigSliderWidget;
import dev.spiritstudios.snapper.gui.widget.FolderSelectWidget;
import dev.spiritstudios.snapper.util.SnapperUtil;
import dev.spiritstudios.snapper.util.uploading.AxolotlClientApi;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
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
                        GalleryScreen.ViewMode.class
                )
        );

        this.list.addSmall(
                booleanButton(
                        "showScreenshotHelper",
                        b -> config.showScreenshotHelper = b,
                        config.showScreenshotHelper
                )
        );

        this.list.addHeader(Component.translatable("config.snapper.panorama"));

        this.list.addBig(
                enumSlider(
                        "panoramaDimensions",
                        b -> config.panoramaDimensions = b,
                        config.panoramaDimensions,
                        SnapperUtil.PanoramaSize.class
                )
        );

        this.list.addBig(
                intSlider(
                        "panoramaSuperSampling",
                        b -> config.superSampling = b,
                        config.superSampling,
                        List.of(1, 2, 3, 4, 5, 6, 7, 8)
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
                Button.builder(CommonComponents.GUI_DONE, _ -> this.onClose())
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

        return CycleButton.builder(
                        boolean_ -> boolean_
                                ? CommonComponents.OPTION_ON
                                : CommonComponents.OPTION_OFF,
                        currentValue
                )
                .withValues(List.of(Boolean.TRUE, Boolean.FALSE))
                .withTooltip(b -> tooltip)
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

        return CycleButton.builder(
                        t -> Component.translatable("config.snapper." + name + "." + t.toString().toLowerCase()),
                        currentValue
                )
                .withValues(Arrays.asList(clazz.getEnumConstants()))
                .withTooltip(b -> tooltip)
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
                values,
                t -> Component.translatable("config.snapper." + name + "." + t.toString().toLowerCase()),
                _ -> tooltip,
                setter
        );
    }

    private AbstractWidget intSlider(
            String name,
            Consumer<Integer> setter,
            int currentValue,
            List<Integer> values
    ) {
        Tooltip tooltip = getTooltip(name);

        return new ConfigSliderWidget<>(
                0, 0,
                150, 20,
                Component.translatable("config.snapper." + name),
                currentValue,
                values,
                t -> Component.translatable("config.snapper." + name + ".value", t),
                _ -> tooltip,
                setter
        );
    }

    private FolderSelectWidget folderSelectWidget(String name, Consumer<Path> setter, Path currentValue) {
        Tooltip tooltip = getTooltip(name);

        FolderSelectWidget widget = new FolderSelectWidget(
                150,
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
        var viewMode = SnapperConfig.HOLDER.get().viewMode();

        config.saveAsync();

        if (lastScreen instanceof ReloadableScreen reloadableScreen) {
            minecraft.submit(reloadableScreen::reload);
            if (config.viewMode != viewMode) {
                reloadableScreen.recreateList();
            }
        }

        minecraft.gui.setScreen(lastScreen);
    }
}
