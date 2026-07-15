package dev.spiritstudios.snapper.gui.toast;

import com.google.common.collect.ImmutableList;
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
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

public class SnapperToast implements Toast {
    private static final Identifier BACKGROUND_SPRITE = Snapper.id("toast/snapper");

    private static final Identifier SCREENSHOT_ICON = Snapper.id("icon/image");
    private static final Identifier PANORAMA_ICON = Snapper.id("icon/panorama");
    private static final Identifier UPLOAD_ICON = Snapper.id("icon/upload");
    private static final Identifier FAILURE_ICON = Snapper.id("icon/nuh_uh");

    private static final int DISPLAY_TIME = 5000;

    private static final int ICON_SIZE = 16;
    private static final int PADDING = 7;

    private final Type type;
    private final List<FormattedCharSequence> titleLines;
    private final List<FormattedCharSequence> messageLines;
    private final int width;

    private Visibility visibility = Visibility.HIDE;

    private static List<FormattedCharSequence> nullToEmpty(final @Nullable Component text) {
        return text == null ? ImmutableList.of() : splitToLength(text);
    }

    private static List<FormattedCharSequence> splitToLength(final Component text) {
        return Minecraft.getInstance().font.split(text, 200);
    }

    public SnapperToast(Type type, Component title, Component message) {
        this.type = type;

        this.titleLines = splitToLength(title);
        this.messageLines = nullToEmpty(message);

        int width = Math.max(160, Stream.concat(this.titleLines.stream(), this.messageLines.stream()).mapToInt(Minecraft.getInstance().font::width).max().orElse(200));
        this.width = width + (PADDING * 2) + (ICON_SIZE);
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

    @Override
    public void update(ToastManager manager, long time) {
        this.visibility = (double) time >= DISPLAY_TIME * manager.getNotificationDisplayTimeMultiplier() ? Visibility.HIDE : Visibility.SHOW;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, Font font, long startTime) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND_SPRITE, 0, 0, this.width(), this.height());
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, getCurrentComponenture(), PADDING, PADDING, ICON_SIZE, ICON_SIZE);

        if (this.messageLines.isEmpty()) {
            this.extractTextLines(graphics, font, this.titleLines, 12, CommonColors.YELLOW);
        } else {
            this.extractTextLines(graphics, font, this.titleLines, PADDING, CommonColors.YELLOW);
            this.extractTextLines(graphics, font, this.messageLines, PADDING + this.titleLines.size() * 12, CommonColors.WHITE);
        }
    }

    private void extractTextLines(GuiGraphicsExtractor graphics, Font font, List<FormattedCharSequence> textLines, int yStart, int textColor) {
        for (int i = 0; i < textLines.size(); i++) {
            graphics.text(font, textLines.get(i), PADDING + 3 + ICON_SIZE, yStart + i * 12, textColor, false);
        }
    }


    public int width() {
        return width;
    }

    public int height() {
        int titleHeight = (this.titleLines.size() - 1) * 12;
        int messageHeight = Math.max(this.messageLines.size(), 1) * 12;
        return ICON_SIZE + PADDING + titleHeight + messageHeight;
    }

    private Identifier getCurrentComponenture() {
        return switch (type) {
            case UPLOAD -> UPLOAD_ICON;
            case PANORAMA -> PANORAMA_ICON;
            case FAILURE -> FAILURE_ICON;
            default -> SCREENSHOT_ICON;
        };
    }

    public enum Type {
        SCREENSHOT, PANORAMA, UPLOAD, FAILURE
    }
}
