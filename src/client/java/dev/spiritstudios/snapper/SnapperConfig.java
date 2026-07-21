package dev.spiritstudios.snapper;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.spiritstudios.snapper.gui.screen.GalleryScreen;
import dev.spiritstudios.snapper.gui.toast.SnapperToasts;
import dev.spiritstudios.snapper.util.DirectoryConfigUtil;
import dev.spiritstudios.snapper.util.SnapperUtil;
import dev.spiritstudios.snapper.util.uploading.AxolotlClientApi;
import lgbt.greenhouse.config.api.v3.GreenhouseConfigHolder;
import lgbt.greenhouse.config.api.v3.GreenhouseConfigSide;
import lgbt.greenhouse.config.api.v3.dfu.builder.DataFixerBuilderFunctions;
import lgbt.greenhouse.config.api.v3.dfu.builder.schema.TypeTemplateBuilder;
import lgbt.greenhouse.config.api.v3.dfu.fix.GreenhouseConfigRelocateFieldsFix;
import lgbt.greenhouse.config.api.v3.dfu.fix.GreenhouseConfigSetFieldsFix;
import lgbt.greenhouse.config.api.v3.lang.GreenhouseConfigJsonCLang;
import lgbt.greenhouse.config.api.v3.lang.GreenhouseConfigJsonLang;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static lgbt.greenhouse.config.api.v3.dfu.fix.GreenhouseConfigSetFieldsFix.function;

public record SnapperConfig(boolean copyTakenScreenshot,
                            SnapperButton snapperButton,
                            GalleryScreen.ViewMode viewMode,
                            Panorama panorama,
                            CustomScreenshotFolder customScreenshotPath,
                            AxolotlClient axolotlClient,
                            boolean showScreenshotHelper) {
    public static final GreenhouseConfigHolder<SnapperConfig> HOLDER = GreenhouseConfigHolder.register(
            SnapperConfig.class,
            Snapper.MOD_ID,
            10200,
            GreenhouseConfigJsonCLang.INSTANCE,
            GreenhouseConfigSide.CLIENT,
            configBuilder -> configBuilder
                    .withValue(
                            "copy_taken_screenshot",
                            "Whether to copy screenshots to the clipboard when taken",
                            Codec.BOOL,
                            false,
                            SnapperConfig::copyTakenScreenshot
                    ).withMapValue(
                            SnapperButton.class,
                            "snapper_button",
                            "Settings relating to the Snapper Button",
                            SnapperConfig::snapperButton,
                            mapBuilder -> mapBuilder
                                    .withValue(
                                            "show_on_title_screen",
                                            "Whether to show Snapper button on title screen",
                                            Codec.BOOL,
                                            true,
                                            SnapperButton::showOnTitleScreen
                                    ).withValue(
                                            "show_in_game_menu",
                                            "Whether to show Snapper button in game menu",
                                            Codec.BOOL,
                                            true,
                                            SnapperButton::showInGameMenu
                                    )
                    ).withValue(
                            "view_mode",
                            """
                                    Whether to show the screenshot menu in a grid or a list
                                    May be either 'grid' or 'list'""",
                            GalleryScreen.ViewMode.CODEC,
                            GalleryScreen.ViewMode.GRID,
                            SnapperConfig::viewMode
                    ).withMapValue(
                            Panorama.class,
                            "panorama",
                            "Settings relating to panoramas",
                            SnapperConfig::panorama,
                            mapBuilder -> mapBuilder
                                    .withValue(
                                            "dimensions",
                                            """
                                                    Dimensions of individual panorama images when saved
                                                    May be any positive power of 2""",
                                            SnapperCodecs.POSITIVE_POWER_OF_2,
                                            1024,
                                            Panorama::dimensions
                                    )
                                    .withValue(
                                            "super_sampling",
                                            """
                                                    How many times to super sample panorama images.
                                                    Increases panorama quality at the cost of rendering time.
                                                    May be any positive integer 8 or below.""",
                                            ExtraCodecs.intRange(1, 8),
                                            4,
                                            Panorama::superSampling
                                    )
                    ).withMapValue(
                            CustomScreenshotFolder.class,
                            "custom_screenshot_path",
                            "Settings relating to a custom screenshot path",
                            SnapperConfig::customScreenshotPath,
                            mapBuilder -> mapBuilder
                                    .withValue(
                                            "enabled",
                                            "Whether to use a custom screenshot path instead of Minecraft's default",
                                            Codec.BOOL,
                                            false,
                                            CustomScreenshotFolder::enabled
                                    ).withValue(
                                            "path",
                                            "The path to use if custom screenshot folders are enabled",
                                            SnapperCodecs.PATH,
                                            SnapperUtil.UNIFIED_FOLDER,
                                            CustomScreenshotFolder::path
                                    )
                    ).withMapValue(
                            AxolotlClient.class,
                            "axolotl_client",
                            "Settings relating to Axolotl Client",
                            SnapperConfig::axolotlClient,
                            mapBuilder -> mapBuilder
                                    .withValue(
                                            "terms_status",
                                            """
                                                    Whether the terms of AxolotlClient have been accepted
                                                    These terms must be accepted to share screenshots via AxolotlClient's image host
                                                    May be 'accept', 'deny', or 'unset'""",
                                            AxolotlClientApi.TermsAcceptance.CODEC,
                                            AxolotlClientApi.TermsAcceptance.UNSET,
                                            AxolotlClient::termsStatus
                                    )
                    ).withValue(
                            "show_screenshot_helper",
                            "Whether to show a screenshot button in the Game Menu",
                            Codec.BOOL,
                            false,
                            SnapperConfig::showScreenshotHelper
                    ),
            dataFixerBuilder -> dataFixerBuilder
                    .withSchema(
                            0,
                            schemaBuilder ->
                                    schemaBuilder
                                            .withField("copyTakenScreenshot", TypeTemplateBuilder.BOOL)
                                            .withField("showSnapperTitleScreen", TypeTemplateBuilder.BOOL)
                                            .withField("showSnapperGameMenu", TypeTemplateBuilder.BOOL)
                                            .withField("viewMode", TypeTemplateBuilder.STRING)
                                            .withField("termsAccepted", TypeTemplateBuilder.STRING)
                                            .withField("panoramaDimensions", TypeTemplateBuilder.STRING)
                                            .withField("useCustomScreenshotFolder", TypeTemplateBuilder.BOOL)
                                            .withField("customScreenshotFolder", TypeTemplateBuilder.STRING)
                    ).withPreviousLang(0, GreenhouseConfigJsonLang.INSTANCE)
                    .withSchemaAndFixes(
                            10100,
                            DataFixerBuilderFunctions.create(
                                    builder -> builder
                                            .withField("copy_taken_screenshot", TypeTemplateBuilder.BOOL)
                                            .withField("snapper_button", TypeTemplateBuilder.map(
                                                    mapBuilder -> mapBuilder
                                                            .withField("show_on_title_screen", TypeTemplateBuilder.BOOL)
                                                            .withField("show_in_game_menu", TypeTemplateBuilder.BOOL)
                                            ))
                                            .withField("view_mode", TypeTemplateBuilder.STRING)
                                            .withField("panorama_dimensions", TypeTemplateBuilder.INT)
                                            .withField("custom_screenshot_path", TypeTemplateBuilder.map(
                                                    mapBuilder -> mapBuilder
                                                            .withField("enabled", TypeTemplateBuilder.BOOL)
                                                            .withField("path", TypeTemplateBuilder.STRING)
                                            ))
                                            .withField("axolotl_client", TypeTemplateBuilder.map(
                                                    mapBuilder -> mapBuilder
                                                            .withField("terms_status", TypeTemplateBuilder.STRING)
                                            ))
                                            .nonRecursive(),
                                    schema -> GreenhouseConfigRelocateFieldsFix.create(
                                            schema,
                                            GreenhouseConfigRelocateFieldsFix.data("copyTakenScreenshot", "copy_taken_screenshot"),
                                            GreenhouseConfigRelocateFieldsFix.data("showSnapperTitleScreen", "snapper_button.show_on_title_screen"),
                                            GreenhouseConfigRelocateFieldsFix.data("showSnapperGameMenu", "snapper_button.show_in_game_menu"),
                                            GreenhouseConfigRelocateFieldsFix.data("viewMode", "view_mode"),
                                            GreenhouseConfigRelocateFieldsFix.data("panoramaDimensions", "panorama_dimensions"),
                                            GreenhouseConfigRelocateFieldsFix.data("useCustomScreenshotFolder", "custom_screenshot_path.enabled"),
                                            GreenhouseConfigRelocateFieldsFix.data("customScreenshotFolder", "custom_screenshot_path.path"),
                                            GreenhouseConfigRelocateFieldsFix.data("termsAccepted", "axolotl_client.terms_status")
                                    )
                            )
                    ).withSchemaAndFixes(
                            10101,
                            DataFixerBuilderFunctions.create(
                                    builder -> builder
                                            .withoutField("panorama_dimensions")
                                            .withField("panorama_size", TypeTemplateBuilder.INT),
                                    schema -> GreenhouseConfigRelocateFieldsFix.create(
                                            schema,
                                            GreenhouseConfigRelocateFieldsFix.data("panorama_dimensions", "panorama_size")
                                    )
                            )
                    ).withSchemaAndFixes(
                            10200,
                            DataFixerBuilderFunctions.create(
                                    builder -> builder
                                            .withoutField("panorama_size")
                                            .withField("panorama", TypeTemplateBuilder.map(
                                                    mapBuilder -> mapBuilder
                                                            .withField("dimensions", TypeTemplateBuilder.INT)
                                            )),
                                    schema -> GreenhouseConfigRelocateFieldsFix.create(
                                            schema,
                                            GreenhouseConfigRelocateFieldsFix.data("panorama_size", "panorama.dimensions")
                                    )
                            ),
                            DataFixerBuilderFunctions.create(
                                    builder -> builder
                                            .withField("panorama", TypeTemplateBuilder.map(
                                                    mapBuilder -> mapBuilder
                                                            .withField("dimensions", TypeTemplateBuilder.INT)
                                                            .withField("super_sampling", TypeTemplateBuilder.INT)
                                            ))
                                            .withField("show_screenshot_helper", TypeTemplateBuilder.BOOL),
                                    schema -> GreenhouseConfigSetFieldsFix.create(
                                            schema,
                                            function("panorama.super_sampling", (_, field) -> field.createInt(4)),
                                            function("show_screenshot_helper", (_, field) -> field.createBoolean(true))
                                    )
                            )
                    )

    );

    public static SnapperConfig get() {
        return HOLDER.get();
    }

    public record SnapperButton(boolean showOnTitleScreen, boolean showInGameMenu) {
    }

    public record CustomScreenshotFolder(boolean enabled, Path path) {
    }

    public record AxolotlClient(AxolotlClientApi.TermsAcceptance termsStatus) {
    }

    public record Panorama(int dimensions, int superSampling) {

    }

    public static void init() {
        HOLDER.reload(null);
    }

    public static Mutable mutable() {
        return new Mutable();
    }

    public static CompletableFuture<Void> editAsync(Consumer<Mutable> editor) {
        Mutable mutable = mutable();
        editor.accept(mutable);

        return mutable.saveAsync();
    }

    public static class Mutable {
        // Root
        public boolean copyTakenScreenshot;
        public GalleryScreen.ViewMode viewMode;
        public boolean showScreenshotHelper;

        // Snapper Button
        public boolean showOnTitleScreen;
        public boolean showInGameMenu;

        // Panorama
        public int panoramaDimensions;
        public int superSampling;

        // Custom Screenshot Folder
        public boolean enabled;
        public Path path;

        // Axolotl Client
        public AxolotlClientApi.TermsAcceptance termsAccepted;

        public Mutable() {
            SnapperConfig config = HOLDER.get();
            copyTakenScreenshot = config.copyTakenScreenshot;
            viewMode = config.viewMode;
            showScreenshotHelper = config.showScreenshotHelper;

            showOnTitleScreen = config.snapperButton.showOnTitleScreen;
            showInGameMenu = config.snapperButton.showInGameMenu;

            panoramaDimensions = config.panorama.dimensions;
            superSampling = config.panorama.superSampling;

            enabled = config.customScreenshotPath.enabled;
            path = config.customScreenshotPath.path;

            termsAccepted = config.axolotlClient.termsStatus;
        }


        public CompletableFuture<Void> saveAsync() {
            SnapperConfig newValue = this.build();

            var oldValue = HOLDER.get();
            HOLDER.set(newValue, null);

            return CompletableFuture.runAsync(
                    () -> HOLDER.save(newValue, null),
                    Util.ioPool()
            ).exceptionallyCompose(error -> Minecraft.getInstance().submit(() -> {
                Snapper.LOGGER.error("Failed to save configuration file.", error);
                SnapperToasts.configSaveFailure();

                HOLDER.set(oldValue, null);
            }));
        }

        private SnapperConfig build() {
            return new SnapperConfig(
                    copyTakenScreenshot,
                    new SnapperButton(
                            showOnTitleScreen,
                            showInGameMenu
                    ),
                    viewMode,
                    new Panorama(
                            panoramaDimensions,
                            superSampling
                    ),
                    new CustomScreenshotFolder(
                            enabled,
                            path
                    ),
                    new AxolotlClient(
                            termsAccepted
                    ),
                    showScreenshotHelper
            );
        }
    }
}
