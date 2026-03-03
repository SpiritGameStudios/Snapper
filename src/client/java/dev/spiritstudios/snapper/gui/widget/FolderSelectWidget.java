package dev.spiritstudios.snapper.gui.widget;

import dev.spiritstudios.snapper.Snapper;
import dev.spiritstudios.snapper.gui.overlay.ExternalDialogOverlay;
import dev.spiritstudios.snapper.util.config.DirectoryConfigUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CommonColors;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

public class FolderSelectWidget extends AbstractContainerWidget implements ContainerEventHandler {
    private static final ResourceLocation FOLDER_ICON = Snapper.id("screenshots/folder");
    private static final ResourceLocation RESET_ICON = Snapper.id("screenshots/reset");

    private static final int BUTTON_WIDTH = 25; // Includes padding

    private final PathFunctions value;

    private final EditBox textInput;
    private final SpriteIconButton fileDialogButton;
    private final SpriteIconButton resetButton;

    public FolderSelectWidget(int x, int y, int width, int height, PathFunctions pathFunctions, String placeholderKey) {
        super(x, y, width, height, CommonComponents.EMPTY);
        this.value = pathFunctions;
        this.active = false;

        Minecraft client = Minecraft.getInstance();

        this.textInput = new EditBox(
                client.font,
                BUTTON_WIDTH * 2, 0,
                width - (BUTTON_WIDTH * 2),
                20,
                Component.literal(pathFunctions.get().toString())
        );

        this.textInput.setHint(Component.translatableWithFallback(placeholderKey, "").withStyle(ChatFormatting.DARK_GRAY));
        this.textInput.setMaxLength(4096); // Unix maximum path length, shorter on windows (I think it may have been 240)
        this.textInput.setValue(pathFunctions.get().toString());
        this.textInput.setResponder(content -> {
            Path path;

            try {
                path = Path.of(content);
            } catch (InvalidPathException ignored) {
                path = null;
            }

            if (path == null || !Files.exists(path) || !Files.isDirectory(path)) {
                this.textInput.setTextColor(CommonColors.RED);
            } else {
                this.textInput.setTextColor(CommonColors.WHITE);
                pathFunctions.set(path);
            }
        });
        this.textInput.setTooltip(Tooltip.create(Component.translatable("config.snapper.customScreenshotFolder.input")));
        this.textInput.moveCursorToStart(false);

        this.fileDialogButton = SpriteIconButton.builder(
                        Component.translatable("config.snapper.customScreenshotFolder.select"),
                        button -> {
                            ExternalDialogOverlay overlay = new ExternalDialogOverlay();
                            client.setOverlay(overlay);

                            DirectoryConfigUtil.openFolderSelect(
                                            Component.translatable("prompt.snapper.folder_select")
                                                    .getString()
                                    )
                                    .thenAccept(path -> {
                                        valueFromSelectDialog(path.orElse(null));
                                        client.submit(overlay::close).join();
                                    });
                        },
                        true
                )
                .width(20)
                .sprite(FOLDER_ICON, 15, 15)
                .build();
        this.fileDialogButton.setTooltip(Tooltip.create(Component.translatable("config.snapper.customScreenshotFolder.select")));

        this.resetButton = SpriteIconButton.builder(
                Component.translatable("config.snapper.customScreenshotFolder.reset"),
                button -> {
                    value.reset();
                    textInput.setValue(value.get().toString());
                },
                true
        ).width(20).sprite(RESET_ICON, 15, 15).build();
        this.resetButton.setTooltip(Tooltip.create(Component.translatable("config.snapper.customScreenshotFolder.reset")));
        resetButton.setX(BUTTON_WIDTH);
    }

    @Override
    public @NonNull List<? extends AbstractWidget> children() {
        return List.of(
                this.fileDialogButton, this.resetButton, this.textInput
        );
    }

    public void setActive(boolean value) {
        this.active = value;

        textInput.setEditable(value);
        textInput.active = value;
        if (textInput.isFocused() && !value) {
            textInput.setFocused(false);
        }

        fileDialogButton.active = value;
        resetButton.active = value;
    }

    @Override
    public void setX(int x) {
        super.setX(x);

        fileDialogButton.setX(x);
        resetButton.setX(x + BUTTON_WIDTH);
        textInput.setX(x + (BUTTON_WIDTH * 2));
    }

    @Override
    public void setY(int y) {
        super.setY(y);

        fileDialogButton.setY(y);
        resetButton.setY(y);
        textInput.setY(y);
    }

    @Override
    public void setWidth(int width) {
        super.setWidth(width);

        textInput.setWidth(width - (BUTTON_WIDTH * 2));
    }

    private void valueFromSelectDialog(@Nullable Path value) {
        if (value == null) {
            return;
        }

        if (Files.exists(value)) {
            this.value.set(value);
            this.textInput.setValue(this.value.get().toString());
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        int clicksRan = 0;

        for (AbstractWidget child : this.children()) {
            if (child.isHovered() && child.isActive()) {
                clicksRan += 1;
                this.playDownSound(Minecraft.getInstance().getSoundManager());

                if (child == textInput) {
                    textInput.setFocused(true);
                    this.setFocused(textInput);
                    textInput.setFocused(true);
                }

                child.onClick(click, doubled);
            }
        }

        return clicksRan == 1;
    }

    @Override
    protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
        for (Renderable drawable : this.children()) {
            drawable.render(context, mouseX, mouseY, deltaTicks);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {
        fileDialogButton.updateWidgetNarration(builder);
        resetButton.updateWidgetNarration(builder);
        textInput.updateWidgetNarration(builder);
    }

    @Override
    public void visitWidgets(Consumer<AbstractWidget> consumer) {
        this.children().forEach(consumer);
    }

    @Override
    protected int contentHeight() {
        return 20;
    }

    @Override
    protected double scrollRate() {
        return 20 / 2f;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        this.active = true;
        var hovered = super.isMouseOver(mouseX, mouseY);
        this.active = false;
        return hovered;
    }

    public static abstract class PathFunctions {
        public abstract Path get();

        public abstract void set(Path path);

        public abstract void reset();
    }
}
