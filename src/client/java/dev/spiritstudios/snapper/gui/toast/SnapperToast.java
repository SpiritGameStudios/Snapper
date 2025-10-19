package dev.spiritstudios.snapper.gui.toast;

import dev.spiritstudios.snapper.Snapper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;

import java.util.List;

public class SnapperToast implements Toast {
    private static final Identifier TEXTURE = Snapper.id("toast/snapper");
    private static final Identifier IMAGE_ICON = Snapper.id("icon/image");
    private static final Identifier UPLOAD_ICON = Snapper.id("icon/upload");
    private static final int VISIBILITY_DURATION = 5000;
    private static final int WIDTH = 256;
    private static final int LINE_HEIGHT = 12;
    private static final int PADDING = 10;

    private final Type type;
    private final Text title;
    private final List<OrderedText> lines;
    private Visibility visibility;

    public SnapperToast(Type type, Text title, Text description) {
        this.type = type;
        this.visibility = Visibility.HIDE;

        this.title = title;
        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;
        this.lines = textRenderer.wrapLines(description, WIDTH - PADDING * 3 - 15);
    }

    @Override
    public Visibility getVisibility() {
        return this.visibility;
    }

    public Visibility setVisibility(Visibility visibility) {
        return this.visibility = visibility;
    }

    @Override
    public void update(ToastManager manager, long time) {
        this.visibility = (double) time >= VISIBILITY_DURATION * manager.getNotificationDisplayTimeMultiplier() ? Visibility.HIDE : Visibility.SHOW;
    }

    @Override
    public void draw(DrawContext context, TextRenderer textRenderer, long startTime) {
        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, 0, 0, this.getWidth(), this.getHeight());
        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, getCurrentTexture(), PADDING, PADDING, 15, 15);

        context.drawText(textRenderer, title, PADDING * 2 + 14, this.lines.isEmpty() ? 12 : 7, Colors.YELLOW, false);

        for (int i = 0; i < this.lines.size(); ++i) {
            context.drawText(textRenderer, this.lines.get(i), PADDING * 2 + 14, 18 + i * 12, Colors.WHITE, false);
        }
    }

    public int getWidth() {
        return WIDTH;
    }

    public int getHeight() {
        return PADDING * 2 + Math.max(this.lines.size(), 1) * LINE_HEIGHT;
    }

    private Identifier getCurrentTexture() {
        switch (type) {
            case UPLOAD -> {
                return UPLOAD_ICON;
            }
            case PANORAMA -> {
                return IMAGE_ICON; // Future proofing. Deal with it.
            }
            default -> {
                return IMAGE_ICON;
            }
        }
    }

    public enum Type {
        SCREENSHOT, PANORAMA, UPLOAD
    }
}
