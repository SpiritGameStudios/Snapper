package dev.spiritstudios.snapper.gui.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.serialization.Codec;
import dev.spiritstudios.snapper.Snapper;
import dev.spiritstudios.snapper.SnapperConfig;
import dev.spiritstudios.snapper.gui.SnapperButtonBar;
import dev.spiritstudios.snapper.gui.toast.SnapperToast;
import dev.spiritstudios.snapper.gui.widget.ScreenshotListWidget;
import dev.spiritstudios.snapper.gui.widget.ScreenshotsWidget;
import dev.spiritstudios.snapper.gui.widget.ViewModeButton;
import dev.spiritstudios.snapper.util.PlatformHelper;
import dev.spiritstudios.snapper.util.ScreenshotActions;
import dev.spiritstudios.snapper.util.SnapperUtil;
import dev.spiritstudios.snapper.util.uploading.ScreenshotUploading;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.nio.file.Path;

public class ScreenshotListScreen extends Screen implements ReloadableScreen {
    private final Screen parent;

    private ScreenshotsWidget screenshots = null;

    private SnapperButtonBar bar;

    private @Nullable ScreenshotListWidget.ScreenshotEntry selectedScreenshot = null;

    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, 33, 60);

    public ScreenshotListScreen(Screen parent) {
        super(Component.translatable("menu.snapper.screenshot_menu"));
        this.parent = parent;
        this.recreateList();
    }

    @Override
    public synchronized void recreateList() {
        if (screenshots != null) {
            this.removeWidget(screenshots);
        }

        screenshots = this.addRenderableWidget(ScreenshotsWidget.create(
                minecraft,
                width,
                height - 48 - 68,
                48,
                screenshots,
                this
        ));

        repositionElements();
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
        screenshots.updateSize(
                width,
                layout
        );
    }

    @Override
    protected void init() {
        this.layout.addTitleHeader(this.title, this.font);

        this.bar = new SnapperButtonBar(
                this,
                this.layout,
                () -> this.selectedScreenshot != null ? this.selectedScreenshot.texture : null,
                true,
                this::toggleGrid,
                this::reload
        );

        this.imageSelected(selectedScreenshot);

        this.layout.visitWidgets(this::addRenderableWidget);

        this.repositionElements();
    }

    public ScreenshotsWidget getScreenshots() {
        return screenshots;
    }

    @Override
    public void onClose() {
        super.onClose();
        screenshots.clearEntries();
    }

    public void imageSelected(@Nullable ScreenshotListWidget.ScreenshotEntry screenshot) {
        boolean hasScreenshot = screenshot != null;
        this.bar.copyButton.active = hasScreenshot;
        this.bar.deleteButton.active = hasScreenshot;
        this.bar.openButton.active = hasScreenshot;
        this.bar.renameButton.active = hasScreenshot;
        this.bar.uploadButton.active = !SnapperUtil.isOfflineAccount() && hasScreenshot;

        this.selectedScreenshot = screenshot;
    }

    public void toggleGrid() {
        SnapperConfig.edit(m -> m.viewMode = SnapperConfig.HOLDER.get().viewMode() == ViewMode.GRID ? ViewMode.LIST : ViewMode.GRID);

        recreateList();
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        if (super.keyPressed(input)) {
            return true;
        }

        if (input.key() == InputConstants.KEY_F5) {
            minecraft.setScreen(new ScreenshotListScreen(this.parent));
            return true;
        }

        if (selectedScreenshot == null) return false;

        if ((input.modifiers() & InputConstants.MOD_CONTROL) != 0 && input.key() == InputConstants.KEY_C) {
            PlatformHelper.INSTANCE.copyScreenshot(selectedScreenshot.texture.path);
            SnapperToast.push(SnapperToast.Type.SCREENSHOT, Component.translatable("toast.snapper.screenshot.copy"), null);
            return true;
        }

        if (input.key() == InputConstants.KEY_RETURN) {
            minecraft.setScreen(new ScreenshotViewerScreen(selectedScreenshot.texture, this));
            return true;
        }

        return false;
    }

    @Override
    public void reload() {
        screenshots.reload();
    }

    public enum ViewMode implements StringRepresentable {
        LIST("list"),
        GRID("grid");

        public static final Codec<ViewMode> CODEC = StringRepresentable.fromEnum(ViewMode::values);

        private final String name;

        ViewMode(String name) {
            this.name = name;
        }

        @Override
        public @NonNull String getSerializedName() {
            return name;
        }
    }
}
