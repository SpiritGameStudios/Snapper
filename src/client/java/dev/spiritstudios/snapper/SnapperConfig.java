package dev.spiritstudios.snapper;

import dev.spiritstudios.snapper.gui.screen.ScreenshotScreen;
import dev.spiritstudios.snapper.util.SnapperUtil;
import dev.spiritstudios.snapper.util.config.DirectoryConfigUtil;
import dev.spiritstudios.snapper.util.uploading.AxolotlClientApi;
import dev.spiritstudios.specter.api.config.Config;
import dev.spiritstudios.specter.api.config.ConfigHolder;
import dev.spiritstudios.specter.api.config.Value;

import java.nio.file.Path;

public final class SnapperConfig extends Config<SnapperConfig> {
    public static final ConfigHolder<SnapperConfig, ?> HOLDER = ConfigHolder.builder(
            Snapper.id("snapper"), SnapperConfig.class
    ).build();

    public static final SnapperConfig INSTANCE = HOLDER.get();

    public final Value<Boolean> copyTakenScreenshot = booleanValue(false)
            .comment("Whether to copy screenshots to clipboard when taken.")
            .build();

    public final Value<Boolean> showSnapperTitleScreen = booleanValue(true)
            .comment("Whether to show Snapper button on title screen.")
            .build();

    public final Value<Boolean> showSnapperGameMenu = booleanValue(true)
            .comment("Whether to show Snapper button in game menu.")
            .build();

    public final Value<ScreenshotScreen.ViewMode> viewMode = enumValue(ScreenshotScreen.ViewMode.GRID, ScreenshotScreen.ViewMode.class)
            .comment("Whether to show screenshot menu with grid or list.")
            .build();

    public final Value<AxolotlClientApi.TermsAcceptance> termsAccepted = enumValue(AxolotlClientApi.TermsAcceptance.UNSET, AxolotlClientApi.TermsAcceptance.class)
            .comment("Whether the terms of AxolotlClient have been accepted.")
            .build();

    public final Value<SnapperUtil.PanoramaSize> panoramaDimensions = enumValue(SnapperUtil.PanoramaSize.ONE_THOUSAND_TWENTY_FOUR, SnapperUtil.PanoramaSize.class)
            .comment("Dimensions of individual panorama images when saved.")
            .build();

    public final Value<Boolean> useCustomScreenshotFolder = booleanValue(false)
            .comment("Whether to use a custom screenshot folder instead of Minecraft's default")
            .build();

    public final Value<Path> customScreenshotFolder = value(SnapperUtil.UNIFIED_FOLDER, DirectoryConfigUtil.PATH_CODEC)
            .comment("What folder to use if custom screenshot folders are enabled.")
            .build();
}
