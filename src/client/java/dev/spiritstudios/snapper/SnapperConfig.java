package dev.spiritstudios.snapper;

import dev.spiritstudios.snapper.gui.screen.ScreenshotScreen;
import dev.spiritstudios.snapper.util.SnapperUtil;
import dev.spiritstudios.snapper.util.uploading.AxolotlClientApi;
import org.polyfrost.oneconfig.api.config.v1.Config;
import org.polyfrost.oneconfig.api.config.v1.annotations.Dropdown;
import org.polyfrost.oneconfig.api.config.v1.annotations.Include;
import org.polyfrost.oneconfig.api.config.v1.annotations.Switch;

import java.nio.file.Path;

public final class SnapperConfig extends Config {

    public static final SnapperConfig INSTANCE = new SnapperConfig();

    @Switch(
            title = "config.snapper.snapper.copyTakenScreenshot",
            description = "config.snapper.snapper.copyTakenScreenshot.tooltip"
    )
    public static boolean copyTakenScreenshot = false;

    @Switch(
            title = "config.snapper.snapper.showSnapperTitleScreen",
            description = "config.snapper.snapper.showSnapperTitleScreen.tooltip"
    )
    public static boolean showSnapperTitleScreen = true;

    @Switch(
            title = "config.snapper.snapper.showSnapperGameMenu",
            description = "config.snapper.snapper.showSnapperGameMenu.tooltip"
    )
    public static boolean showSnapperGameMenu = true;

    @Dropdown(
            title = "config.snapper.snapper.viewMode",
            description = "config.snapper.snapper.viewMode.tooltip"
    )
    public static ScreenshotScreen.ViewMode viewMode = ScreenshotScreen.ViewMode.GRID;

    @Dropdown(
            title = "config.snapper.snapper.termsAccepted",
            description = "config.snapper.snapper.termsAccepted.tooltip"
    )
    public static AxolotlClientApi.TermsAcceptance termsAccepted = AxolotlClientApi.TermsAcceptance.UNSET;

    @Switch(
            title = "config.snapper.snapper.useCustomScreenshotFolder",
            description = "config.snapper.snapper.useCustomScreenshotFolder.tooltip"
    )
    public static boolean useCustomScreenshotFolder = false;

    @Include public static Path customScreenshotFolder = SnapperUtil.UNIFIED_FOLDER; //todo replace with a "file" option

    public SnapperConfig() {
        super("snapper.json", "/assets/snapper/icon.png", "Snapper", Category.QOL);
    }
}
