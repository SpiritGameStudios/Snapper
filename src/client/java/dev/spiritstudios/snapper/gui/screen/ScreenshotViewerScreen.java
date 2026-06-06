package dev.spiritstudios.snapper.gui.screen;

import dev.spiritstudios.snapper.util.PlatformHelper;
import dev.spiritstudios.snapper.util.ScreenshotActions;
import dev.spiritstudios.snapper.util.ScreenshotTexture;
import dev.spiritstudios.snapper.util.SnapperUtil;
import dev.spiritstudios.snapper.util.uploading.ScreenshotUploading;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;

public class ScreenshotViewerScreen extends Screen implements ReloadableScreen {
    private static final Identifier MENU_LIST_BACKGROUND = Identifier.withDefaultNamespace("textures/gui/menu_list_background.png");
    private static final Identifier INWORLD_MENU_LIST_BACKGROUND = Identifier.withDefaultNamespace("textures/gui/inworld_menu_list_background.png");

    private final Minecraft client = Minecraft.getInstance();
    private final ScreenshotTexture texture;

    private final Screen parent;
    private final @Nullable List<Path> screenshots;
    private final int screenshotIndex;
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, 33, 60);

    public boolean shouldReloadParent = false;

    public ScreenshotViewerScreen(ScreenshotTexture texture, Screen parent) {
        this(texture, parent, null);
    }

    public ScreenshotViewerScreen(ScreenshotTexture texture, Screen parent, @Nullable List<Path> screenshots) {
        super(Component.literal(texture.path.getFileName().toString()));
        this.parent = parent;

        this.texture = texture;
        texture.startLoading(Minecraft.getInstance(), true);

        this.screenshots = screenshots;

        this.screenshotIndex = this.screenshots != null ? this.screenshots.indexOf(this.texture.path) : -1;
    }

    @Override
    public void onClose() {
        if (!(parent instanceof ScreenshotListScreen listScreen)) {
            this.texture.close();
        } else if (shouldReloadParent) {
            listScreen.getScreenshots().reload();
        }

        this.client.setScreen(this.parent);
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
    }

    @Override
    protected void init() {
        // TODO: Dedupe code from here and ScreenshotListScreen
        this.layout.addTitleHeader(this.title, this.font);

        final int hSpacing = 4;

        final int buttonWidth = 74;
        final int bottomButtonWidth = 100;

        LinearLayout vertical = this.layout.addToFooter(LinearLayout.vertical().spacing(4));
        vertical.defaultCellSetting().alignHorizontallyCenter();

        LinearLayout topRow = vertical.addChild(LinearLayout.horizontal().spacing(hSpacing));
        LinearLayout bottomRow = vertical.addChild(LinearLayout.horizontal().spacing(hSpacing));

        bottomRow.addChild(Button.builder(
                Component.translatable("button.snapper.folder"),
                button -> Util.getPlatform().openPath(SnapperUtil.getConfiguredScreenshotDirectory())
        ).width(bottomButtonWidth).build());

        bottomRow.addChild(Button.builder(
                Component.translatable("button.snapper.open"),
                button -> {
                    Util.getPlatform().openPath(this.texture.path);
                }
        ).width(bottomButtonWidth).build());

        bottomRow.addChild(Button.builder(
                CommonComponents.GUI_DONE,
                button -> this.onClose()
        ).width(bottomButtonWidth).build());

        topRow.addChild(Button.builder(
                Component.translatable("button.snapper.delete"),
                button -> {
                    ScreenshotActions.deleteScreenshot(this.texture.path, parent);
                }
        ).width(buttonWidth).build());

        topRow.addChild(Button.builder(
                Component.translatable("button.snapper.rename"),
                button -> {
                    minecraft.setScreen(new ScreenshotRenameScreen(this.texture.path, this));
                }
        ).width(buttonWidth).build());

        topRow.addChild(Button.builder(
                Component.translatable("button.snapper.copy"),
                button -> {
                    PlatformHelper.INSTANCE.copyScreenshot(this.texture.path);
                }
        ).width(buttonWidth).build());

        var uploadButton = topRow.addChild(Button.builder(Component.translatable("button.snapper.upload"), button -> {
            button.active = false;
            ScreenshotUploading.upload(this.texture.path)
                    .thenRun(() -> button.active = true);
        }).width(buttonWidth).build());

        if (SnapperUtil.isOfflineAccount()) {
            uploadButton.active = false;
            uploadButton.setTooltip(Tooltip.create(Component.translatable("button.snapper.upload.tooltip")));
        }

        this.layout.visitWidgets(this::addRenderableWidget);
        this.repositionElements();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);

        this.drawMenuBackground(graphics);
        this.drawHeaderAndFooterSeparators(graphics);

        int finalHeight = layout.getContentHeight();
        float scaleFactor = (float) finalHeight / texture.getHeight();
        int finalWidth = (int) (texture.getWidth() * scaleFactor);

        if (texture.isLoaded()) {
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    this.texture.textureLocation(),
                    (this.width / 2) - (finalWidth / 2), layout.getHeaderHeight(),
                    0, 0,
                    finalWidth, finalHeight,
                    finalWidth, finalHeight
            );
        }

        if (screenshotIndex != -1 && screenshots != null) {
            graphics.centeredText(
                    this.font,
                    "Screenshot %d/%d".formatted(screenshotIndex + 1, screenshots.size()),
                    this.width / 2,
                    30,
                    CommonColors.WHITE
            );
        }

        // TODO: Maybe add an option to the debug menu to turn this off
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            graphics.centeredText(
                    this.font,
                    Component.translatable("text.snapper.image_size", texture.getWidth(), texture.getHeight()),
                    this.width / 2,
                    40,
                    CommonColors.WHITE
            );

            graphics.centeredText(
                    this.font,
                    Component.translatable("text.snapper.screen_size", this.width, this.height),
                    this.width / 2,
                    50,
                    CommonColors.WHITE
            );

            graphics.centeredText(this.font,
                    Component.translatable("text.snapper.scale_factor", scaleFactor),
                    this.width / 2,
                    60,
                    CommonColors.WHITE
            );

            graphics.centeredText(
                    this.font,
                    Component.translatable("text.snapper.scale_size", finalWidth, finalHeight),
                    this.width / 2,
                    70,
                    CommonColors.WHITE
            );
        }
    }

    private void drawMenuBackground(GuiGraphicsExtractor graphics) {
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                this.minecraft.level == null ? MENU_LIST_BACKGROUND : INWORLD_MENU_LIST_BACKGROUND,
                0,
                layout.getHeaderHeight(),
                0, 0,
                width,
                layout.getContentHeight(),
                32,
                32
        );
    }

    private void drawHeaderAndFooterSeparators(GuiGraphicsExtractor graphics) {
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                this.minecraft.level == null ? Screen.HEADER_SEPARATOR : Screen.INWORLD_HEADER_SEPARATOR,
                0, layout.getHeaderHeight() - 2,
                0.0F, 0.0F,
                width, 2,
                32, 2
        );

        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                this.minecraft.level == null ? Screen.FOOTER_SEPARATOR : Screen.INWORLD_FOOTER_SEPARATOR,
                0, layout.getHeaderHeight() + layout.getContentHeight(),
                0.0F, 0.0F,
                width, 2,
                32, 2
        );
    }

    @Override
    public void reload() {
        this.shouldReloadParent = true;
    }
}
