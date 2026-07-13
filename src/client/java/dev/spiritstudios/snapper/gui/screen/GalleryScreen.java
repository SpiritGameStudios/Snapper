package dev.spiritstudios.snapper.gui.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.serialization.Codec;
import dev.spiritstudios.snapper.SnapperConfig;
import dev.spiritstudios.snapper.gui.SnapperButtonBar;
import dev.spiritstudios.snapper.gui.toast.SnapperToast;
import dev.spiritstudios.snapper.gui.widget.GalleryWidget;
import dev.spiritstudios.snapper.util.PlatformHelper;
import dev.spiritstudios.snapper.util.ScreenshotActions;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.client.gui.components.tabs.MenuTabBar;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class GalleryScreen extends Screen implements ReloadableScreen {
    private static final Component SCREENSHOTS_BUTTON = Component.translatable("menu.snapper.tab.screenshots");
    private static final Component PANORAMAS_BUTTON = Component.translatable("menu.snapper.tab.panoramas");

    private final Screen parent;

    private SnapperButtonBar bar;

    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, 33, 60);
    private final TabManager tabManager = new TabManager(
            this::addRenderableWidget,
            this::removeWidget,
            _ -> {},
            _ -> setSelected(null)
    );
    private @Nullable MenuTabBar tabNavigationBar;

    private @Nullable GalleryWidget screenshots;
    private @Nullable GalleryWidget panoramas;
    private GalleryWidget.ScreenshotEntry selected = null;

    public GalleryScreen(Screen parent) {
        super(Component.translatable("menu.snapper.screenshot_menu"));
        this.parent = parent;
    }

    private final class GalleryTab extends GridLayoutTab {
        private final GalleryWidget gallery;

        public GalleryTab(final Component title, final GalleryWidget gallery) {
            super(title);
            this.layout.addChild(gallery, 1, 1);
            this.gallery = gallery;
        }

        @Override
        public void doLayout(final ScreenRectangle screenRectangle) {
            this.gallery.updateSizeAndPosition(GalleryScreen.this.width, GalleryScreen.this.layout.getContentHeight(), GalleryScreen.this.layout.getHeaderHeight());
            super.doLayout(screenRectangle);
        }
    }

    @Override
    protected void repositionElements() {
        if (this.tabNavigationBar != null) {
            this.tabNavigationBar.arrangeElements(this.width);
            int tabAreaTop = this.tabNavigationBar.getRectangle().bottom();
            ScreenRectangle tabArea = new ScreenRectangle(0, tabAreaTop, this.width, this.height - this.layout.getFooterHeight() - tabAreaTop);
            this.tabNavigationBar.getTabs().forEach(tab -> tab.visitChildren(child -> child.setHeight(tabArea.height())));
            this.tabManager.setTabArea(tabArea);
            this.layout.setHeaderHeight(tabAreaTop);
            this.layout.arrangeElements();
        }
    }

    @Override
    protected void init() {
        this.screenshots = GalleryWidget.create(
                minecraft,
                this.layout.getWidth(), this.layout.getContentHeight(),
                0,
                () -> ScreenshotActions.getScreenshotTextures(minecraft.getTextureManager()),
                screenshots,
                this
        );

        this.panoramas = GalleryWidget.create(
                minecraft,
                this.layout.getWidth(), this.layout.getContentHeight(),
                0,
                () -> ScreenshotActions.getPanoramaTextures(minecraft.getTextureManager()),
                panoramas,
                this
        );

        int currentTabIndex = this.tabNavigationBar != null ?
                tabNavigationBar.getTabs().indexOf(tabManager.getCurrentTab()) :
                0;

        this.tabNavigationBar = MenuTabBar.builder(this.tabManager, this.width)
                .addTabs(
                        new GalleryTab(SCREENSHOTS_BUTTON, screenshots),
                        new GalleryTab(PANORAMAS_BUTTON, panoramas)
                )
                .build();

        this.addRenderableWidget(this.tabNavigationBar);

        this.bar = new SnapperButtonBar(
                this,
                this,
                this.layout,
                () -> this.selected != null ? this.selected.texture : null,
                true,
                this::toggleGrid,
                this::reload
        );

        this.layout.visitWidgets(button -> {
            button.setTabOrderGroup(1);
            this.addRenderableWidget(button);
        });
        this.tabNavigationBar.selectTab(currentTabIndex, false);

        this.repositionElements();
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(this.parent);
        if (screenshots != null) screenshots.clearEntries();
        if (panoramas != null) panoramas.clearEntries();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);
        graphics.blit(RenderPipelines.GUI_TEXTURED, Screen.FOOTER_SEPARATOR, 0, this.height - this.layout.getFooterHeight(), 0.0F, 0.0F, this.width, 2, 32, 2);
    }

    @Override
    protected void extractMenuBackground(final GuiGraphicsExtractor graphics) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, CreateWorldScreen.TAB_HEADER_BACKGROUND, 0, 0, 0.0F, 0.0F, this.width, this.layout.getHeaderHeight(), 16, 16);
        this.extractMenuBackground(graphics, 0, this.layout.getHeaderHeight(), this.width, this.height);
    }

    public void setSelected(GalleryWidget.ScreenshotEntry entry) {
        boolean hasScreenshot = entry != null;
        this.bar.copyButton.active = hasScreenshot;
        this.bar.deleteButton.active = hasScreenshot;
        this.bar.openButton.active = hasScreenshot;
        this.bar.renameButton.active = hasScreenshot;
        this.bar.setUploadButtonActive(hasScreenshot);

        this.selected = entry;
    }

    public void toggleGrid() {
        SnapperConfig.editAsync(m -> m.viewMode = SnapperConfig.HOLDER.get().viewMode() == ViewMode.GRID ? ViewMode.LIST : ViewMode.GRID);

        recreateList();
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        if (super.keyPressed(input)) {
            return true;
        }

        if (input.key() == InputConstants.KEY_F5) {
            minecraft.gui.setScreen(new GalleryScreen(this.parent));
            return true;
        }

        if (selected == null) return false;

        if ((input.modifiers() & InputConstants.MOD_CONTROL) != 0 && input.key() == InputConstants.KEY_C) {
            PlatformHelper.INSTANCE.copyScreenshot(selected.texture.path);
            SnapperToast.push(SnapperToast.Type.SCREENSHOT, Component.translatable("toast.snapper.screenshot.copy"), null);
            return true;
        }

        if (input.key() == InputConstants.KEY_RETURN) {
            minecraft.gui.setScreen(selected.texture.createViewer(parent));
            return true;
        }

        return false;
    }

    @Override
    public void recreateList() {
        this.rebuildWidgets();
    }

    @Override
    public void reload() {
        if (screenshots != null) screenshots.reload();
        if (panoramas != null) panoramas.reload();
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
