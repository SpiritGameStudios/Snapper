package dev.spiritstudios.snapper.gui.toast;

import dev.spiritstudios.snapper.Snapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CommonColors;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public class SnapperToast implements Toast {
    private static final ResourceLocation TEXTURE = Snapper.id("toast/snapper");
    private static final ResourceLocation SCREENSHOT_ICON = Snapper.id("icon/image");
    private static final ResourceLocation PANORAMA_ICON = Snapper.id("icon/panorama");
    private static final ResourceLocation UPLOAD_ICON = Snapper.id("icon/upload");
    private static final ResourceLocation DENY_ICON = Snapper.id("icon/nuh_uh");
    private static final int VISIBILITY_DURATION = 5000;
    private static final int TEXT_WIDTH = 192;
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
        this.lines = textRenderer.split(description, TEXT_WIDTH - PADDING * 3 - 16);
    }

    public static void push(Type type, Component title, Component description) {
        Minecraft.getInstance().getToastManager().addToast(
                new SnapperToast(
                        type,
                        title,
                        description
                )
        );
    }

    @Override
    public Visibility getWantedVisibility() {
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
    public void render(GuiGraphics context, Font font, long startTime) {
        context.blitSprite(RenderPipelines.GUI_TEXTURED, TEXTURE, 0, 0, this.width(), this.height());
        context.blitSprite(RenderPipelines.GUI_TEXTURED, getCurrentComponenture(), PADDING, PADDING, 16, 16);

        context.drawString(font, title, PADDING * 2 + 12, this.lines.isEmpty() ? 12 : 7, CommonColors.YELLOW, false);

        for (int i = 0; i < this.lines.size(); ++i) {
            context.drawString(font, this.lines.get(i), PADDING * 2 + 12, 18 + i * 12, CommonColors.WHITE, false);
        }
    }

    public int width() {
        Font font = Minecraft.getInstance().font;
        return PADDING + lines.stream().mapToInt(font::width)
                .max()
                .orElse(202) + 28;
    }

    public int height() {
        return PADDING * 2 + Math.max(this.lines.size(), 1) * LINE_HEIGHT;
    }

    private ResourceLocation getCurrentComponenture() {
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
