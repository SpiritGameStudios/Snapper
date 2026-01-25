package dev.spiritstudios.snapper;

import com.mojang.serialization.Codec;
import dev.spiritstudios.snapper.gui.screen.ScreenshotScreen;
import dev.spiritstudios.snapper.util.SnapperUtil;
import dev.spiritstudios.snapper.util.config.DirectoryConfigUtil;
import dev.spiritstudios.snapper.util.uploading.AxolotlClientApi;
import lgbt.greenhouse.config.api.v3.GreenhouseConfigSide;
import lgbt.greenhouse.config.api.v3.config.GreenhouseConfigHolder;
import lgbt.greenhouse.config.api.v3.dfu.builder.DataFixerBuilderFunctions;
import lgbt.greenhouse.config.api.v3.dfu.builder.schema.TypeTemplateBuilder;
import lgbt.greenhouse.config.api.v3.dfu.fix.GreenhouseConfigRelocateFieldsFix;
import lgbt.greenhouse.config.api.v3.lang.GreenhouseConfigJsonCLang;
import lgbt.greenhouse.config.api.v3.lang.GreenhouseConfigJsonLang;

import java.nio.file.Path;
import java.util.Map;

public record SnapperConfig(boolean copyTakenScreenshot,
                            SnapperButton snapperButton,
                            ScreenshotScreen.ViewMode viewMode,
                            SnapperUtil.PanoramaSize panoramaDimensions,
                            CustomScreenshotFolder customScreenshotPath,
                            AxolotlClient axolotlClient) {
    public static final GreenhouseConfigHolder<SnapperConfig> HOLDER = GreenhouseConfigHolder.register(
            SnapperConfig.class,
            "snapper",
            10100,
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
                            ScreenshotScreen.ViewMode.CODEC,
                            ScreenshotScreen.ViewMode.GRID,
                            SnapperConfig::viewMode
                    ).withValue(
                            "panorama_size",
                            """
                                    Dimensions of individual panorama images when saved
                                    May be 1024, 2048, or 4096""",
                            SnapperUtil.PanoramaSize.CODEC,
                            SnapperUtil.PanoramaSize.ONE_THOUSAND_TWENTY_FOUR,
                            SnapperConfig::panoramaDimensions
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
                                            DirectoryConfigUtil.PATH_CODEC,
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
                                            .withField("panorama_dimensions", TypeTemplateBuilder.STRING)
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
                                            GreenhouseConfigRelocateFieldsFix.function("copyTakenScreenshot", "copy_taken_screenshot"),
                                            GreenhouseConfigRelocateFieldsFix.function("showSnapperTitleScreen", "snapper_button.show_on_title_screen"),
                                            GreenhouseConfigRelocateFieldsFix.function("showSnapperGameMenu", "snapper_button.show_in_game_menu"),
                                            GreenhouseConfigRelocateFieldsFix.function("viewMode", "view_mode"),
                                            GreenhouseConfigRelocateFieldsFix.function("panoramaDimensions", "panorama_dimensions"),
                                            GreenhouseConfigRelocateFieldsFix.function("useCustomScreenshotFolder", "custom_screenshot_path.enabled"),
                                            GreenhouseConfigRelocateFieldsFix.function("customScreenshotFolder", "custom_screenshot_path.path"),
                                            GreenhouseConfigRelocateFieldsFix.function("termsAccepted", "axolotl_client.terms_status")
                                    )
                            )
                    )
    );

    public record SnapperButton(boolean showOnTitleScreen, boolean showInGameMenu) {}
    public record CustomScreenshotFolder(boolean enabled, Path path) {}
    public record AxolotlClient(AxolotlClientApi.TermsAcceptance termsStatus) {}

    public static void init() {}
    public static Mutable mutable() {
        return new Mutable();
    }

    public static class Mutable {
        // Root
        public boolean copyTakenScreenshot;
        public ScreenshotScreen.ViewMode viewMode;
        public SnapperUtil.PanoramaSize panoramaDimensions;

        // Snapper Button
        public boolean showOnTitleScreen;
        public boolean showInGameMenu;

        // Custom Screenshot Folder
        public boolean enabled;
        public Path path;

        // Axolotl Client
        public AxolotlClientApi.TermsAcceptance termsAccepted;

        public Mutable() {
            SnapperConfig config = HOLDER.get();
            copyTakenScreenshot = config.copyTakenScreenshot;
            viewMode = config.viewMode;
            panoramaDimensions = config.panoramaDimensions;

            showOnTitleScreen = config.snapperButton.showOnTitleScreen;
            showInGameMenu = config.snapperButton.showInGameMenu;

            enabled = config.customScreenshotPath.enabled;
            path = config.customScreenshotPath.path;

            termsAccepted = config.axolotlClient.termsStatus;
        }

        public void save() {
            HOLDER.save(build(), null);
        }

        private SnapperConfig build() {
            return new SnapperConfig(
                    copyTakenScreenshot,
                    new SnapperButton(
                            showOnTitleScreen,
                            showInGameMenu
                    ),
                    viewMode,
                    panoramaDimensions,
                    new CustomScreenshotFolder(
                            enabled,
                            path
                    ),
                    new AxolotlClient(
                            termsAccepted
                    )
            );
        }
    }
}
