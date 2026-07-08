package dev.spiritstudios.snapper.gui.toast;

import dev.spiritstudios.snapper.Snapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.util.FormattedCharSequence;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class SnapperToast implements Toast {
    private static final Identifier TEXTURE = Snapper.id("toast/snapper");
    private static final Identifier SCREENSHOT_ICON = Snapper.id("icon/image");
    private static final Identifier PANORAMA_ICON = Snapper.id("icon/panorama");
    private static final Identifier UPLOAD_ICON = Snapper.id("icon/upload");
    private static final Identifier DENY_ICON = Snapper.id("icon/nuh_uh");
    private static final int VISIBILITY_DURATION = 5000;
    private static final int WIDTH = 256;
    private static final int LINE_HEIGHT = 12;
    private static final int PADDING = 10;

    private final Type type;
    private final Component title;
    private final List<FormattedCharSequence> lines;
    private Visibility visibility;

    public SnapperToast(Type type, Component title, Component description) {
        this.type = type;
        this.visibility = Visibility.HIDE;

        this.title = title;
        Minecraft minecraft = Minecraft.getInstance();
        Font textRenderer = minecraft.font;
        this.lines = textRenderer.split(description, WIDTH - PADDING * 3 - 16);
    }

    public static void push(Type type, Component title, Component description) {
        Minecraft.getInstance().gui.toastManager().addToast(
                new SnapperToast(
                        type,
                        title,
                        description
                )
        );
    }

    @Override
    public @NonNull Visibility getWantedVisibility() {
        return this.visibility;
    }

    public Visibility setWantedVisibility(Visibility visibility) {
        return this.visibility = visibility;
    }

    @Override
    public void update(ToastManager manager, long time) {
        this.visibility = (double) time >= VISIBILITY_DURATION * manager.getNotificationDisplayTimeMultiplier() ? Visibility.HIDE : Visibility.SHOW;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, Font font, long startTime) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, TEXTURE, 0, 0, this.width(), this.height());
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, getCurrentComponenture(), PADDING, PADDING, 16, 16);

        graphics.text(font, title, PADDING * 2 + 12, this.lines.isEmpty() ? 12 : 7, CommonColors.YELLOW, false);

        for (int i = 0; i < this.lines.size(); ++i) {
            graphics.text(font, this.lines.get(i), PADDING * 2 + 12, 18 + i * 12, CommonColors.WHITE, false);
        }
    }

    public int width() {
        return WIDTH;
    }

    public int height() {
        return PADDING * 2 + Math.max(this.lines.size(), 1) * LINE_HEIGHT;
    }

    private Identifier getCurrentComponenture() {
        return switch (type) {
            case UPLOAD -> UPLOAD_ICON;
            case PANORAMA -> PANORAMA_ICON;
            case DENY -> DENY_ICON;
            default -> SCREENSHOT_ICON;
        };
    }

    public enum Type {
        SCREENSHOT, PANORAMA, UPLOAD, DENY
    }
}
