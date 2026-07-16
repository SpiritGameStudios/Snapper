package dev.spiritstudios.snapper.gui.screen;

import dev.spiritstudios.snapper.gui.SnapperButtonBar;
import dev.spiritstudios.snapper.render.texture.ScreenshotTexture;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;

public class ScreenshotViewerScreen extends Screen implements ReloadableScreen {
    private static final Identifier MENU_LIST_BACKGROUND = Identifier.withDefaultNamespace("textures/gui/menu_list_background.png");
    private static final Identifier INWORLD_MENU_LIST_BACKGROUND = Identifier.withDefaultNamespace("textures/gui/inworld_menu_list_background.png");

    private final ScreenshotTexture texture;

    private final Screen parent;
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, 33, 60);

    public boolean shouldReloadParent = false;
    public boolean shouldRecreateParent = false;

    public ScreenshotViewerScreen(ScreenshotTexture texture, Screen parent) {
        this(texture, parent, null);
    }

    public ScreenshotViewerScreen(ScreenshotTexture texture, Screen parent, @Nullable List<Path> screenshots) {
        super(Component.literal(texture.path.getFileName().toString()));
        this.parent = parent;

        this.texture = texture;
        texture.startLoading(Minecraft.getInstance(), true);
    }

    @Override
    public void onClose() {
        if (!(parent instanceof GalleryScreen)) {
            this.texture.close();
        }

        if (parent instanceof ReloadableScreen reloadable) {
            if (shouldReloadParent) reloadable.reload();
            if (shouldRecreateParent) reloadable.recreateList();
        }

        this.minecraft.gui.setScreen(this.parent);
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
    }

    @Override
    protected void init() {
        this.layout.addTitleHeader(this.title, this.font);

        new SnapperButtonBar(
                this,
                this.parent,
                this.layout,
                () -> this.texture,
                null,
                null
        );

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
                    this.texture.textureLocation,
                    (this.width / 2) - (finalWidth / 2), layout.getHeaderHeight(),
                    0, 0,
                    finalWidth, finalHeight,
                    finalWidth, finalHeight
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

    @Override
    public void recreateList() {
        this.shouldRecreateParent = true;
    }
}
