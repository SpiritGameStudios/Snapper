package dev.spiritstudios.snapper.gui.screen.viewer;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import dev.spiritstudios.snapper.gui.SnapperButtonBar;
import dev.spiritstudios.snapper.gui.screen.GalleryScreen;
import dev.spiritstudios.snapper.gui.screen.ParentReloaderScreen;
import dev.spiritstudios.snapper.render.texture.PanoramaTexture;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.CubeMap;
import net.minecraft.client.renderer.Panorama;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.Optional;

public class PanoramaViewerScreen extends ParentReloaderScreen {
    private final PanoramaTexture texture;

    private static final float DRAG = 1.0F;
    private static final float SPEED = 30F; // maybe add to config - SkyNotTheLimit

    private boolean isHeld = false;

    private float spin = 0.0F;
    private float velocity = 0.0F;

    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, HeaderAndFooterLayout.DEFAULT_HEADER_AND_FOOTER_HEIGHT, 60);

    public PanoramaViewerScreen(PanoramaTexture texture, Screen parent) {
        super(Component.literal(texture.path.getFileName().toString()), parent);
        this.texture = texture;
    }

    @Override
    public void onClose() {
        if (!(parent instanceof GalleryScreen)) this.texture.close();
        super.onClose();
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
    }

    @Override
    protected void init() {
        this.layout.addTitleHeader(this.title, this.font);


        SnapperButtonBar bar = new SnapperButtonBar(
                this,
                this.parent,
                () -> this.texture,
                null,
                null
        );

        layout.addToFooter(bar.layout);

        this.layout.visitWidgets(this::addRenderableWidget);
        this.repositionElements();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        boolean result = super.mouseClicked(event, doubleClick);
        Optional<GuiEventListener> child = this.getChildAt(event.x(), event.y());

        if (event.button() == 0 && child.isEmpty()) {
            this.isHeld = true;
        }

        return result;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0) this.isHeld = false;
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (event.buttonInfo().button() == 0) {
            this.velocity += (float) ((dx * SPEED) / CubeMap.PROJECTION_FOV);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        float aRealTime = minecraft.getDeltaTracker().getRealtimeDeltaTicks();
        float delta = (float) ((double) aRealTime * minecraft.gameRenderer.gameRenderState().optionsRenderState.panoramaSpeed);

        float constantSpin = isHeld ? 0.0F : delta * 0.1F;
        float velocityPerSecond = velocity * aRealTime;

        this.spin = Mth.wrapDegrees(this.spin + constantSpin - velocityPerSecond);

        final float dragIsALie = isHeld ? 1.0F : DRAG;

        this.velocity -= (velocity / dragIsALie) * aRealTime;

        graphics.requestCursor(CursorTypes.RESIZE_EW);

        texture.startLoading(minecraft, false);
        if (texture.isLoaded()) {
            graphics.snapper$panorama(
                    -this.spin,
                    this.texture.textureLocation,
                    0, 0,
                    graphics.guiWidth(), graphics.guiHeight()
            );

            graphics.blit(RenderPipelines.GUI_TEXTURED, Panorama.PANORAMA_OVERLAY, 0, 0, 0.0F, 0.0F, width, height, 16, 128, 16, 128);
        }

        super.extractRenderState(graphics, mouseX, mouseY, a);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
    }
}
