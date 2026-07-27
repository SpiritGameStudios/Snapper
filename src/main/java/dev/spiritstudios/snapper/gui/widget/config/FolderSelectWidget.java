package dev.spiritstudios.snapper.gui.widget.config;

import dev.spiritstudios.snapper.Snapper;
import dev.spiritstudios.snapper.gui.overlay.ExternalDialogOverlay;
import joptsimple.internal.Strings;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import org.apache.commons.lang3.SystemProperties;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class FolderSelectWidget extends AbstractContainerWidget {
    private static final Identifier FOLDER_ICON = Snapper.id("screenshots/folder");
    private static final Identifier RESET_ICON = Snapper.id("screenshots/reset");

    private static final int BUTTON_SIZE = 20;
    private static final int PADDING = 3;

    private final EditBox editBox;
    private final SpriteIconButton resetButton;
    private final SpriteIconButton fileDialogButton;
    private final Minecraft minecraft = Minecraft.getInstance();
    private final LinearLayout layout;
    private final PathFunctions functions;

    public FolderSelectWidget(int width, PathFunctions pathFunctions, String placeholderKey) {
        super(0, 0, width, 0, CommonComponents.EMPTY);
        this.functions = pathFunctions;
        this.active = false;

        this.layout = LinearLayout.horizontal().spacing(3);
        this.editBox = new EditBox(
                minecraft.font, width - ((BUTTON_SIZE + PADDING) * 2), 20,
                Component.literal(pathFunctions.get().toString())
        );

        this.editBox.setHint(Component.translatableWithFallback(placeholderKey, "").withStyle(ChatFormatting.DARK_GRAY));
        this.editBox.setMaxLength(4096); // Unix maximum path length, shorter on windows (I think it may have been 240)
        this.editBox.setValue(pathFunctions.get().toString());
        this.editBox.setResponder(content -> {
            Path path;

            try {
                path = Path.of(content);
            } catch (InvalidPathException ignored) {
                path = null;
            }

            if (path == null || !Files.exists(path) || !Files.isDirectory(path)) {
                this.editBox.setTextColor(CommonColors.RED);
            } else {
                this.editBox.setTextColor(CommonColors.WHITE);
                pathFunctions.set(path);
            }
        });
        this.editBox.setTooltip(Tooltip.create(Component.translatable("config.snapper.customScreenshotFolder.input")));
        this.editBox.moveCursorToStart(false);

        this.fileDialogButton = SpriteIconButton.builder(
                        Component.translatable("config.snapper.customScreenshotFolder.select"),
                        _ -> {
                            ExternalDialogOverlay overlay = new ExternalDialogOverlay();
                            minecraft.gui.setOverlay(overlay);

                            // replaceAll is to prevent an ACE exploit in TinyFD
                            CompletableFuture.supplyAsync(() -> TinyFileDialogs.tinyfd_selectFolderDialog(
                                            Component.translatable("prompt.snapper.folder_select")
                                                    .getString()
                                                    .replaceAll("[^a-zA-Z0-9 .,]", ""),
                                            SystemProperties.getUserHome()
                                    ))
                                    .thenCompose(selectedPath -> {
                                        if (Strings.isNullOrEmpty(selectedPath)) {
                                            valueFromSelectDialog(null);
                                        } else {
                                            valueFromSelectDialog(Path.of(selectedPath));
                                        }

                                        return minecraft.submit(overlay::close);
                                    });
                        },
                        true
                )
                .width(BUTTON_SIZE)
                .sprite(FOLDER_ICON, 15, 15)
                .build();
        this.fileDialogButton.setTooltip(Tooltip.create(Component.translatable("config.snapper.customScreenshotFolder.select")));

        this.resetButton = SpriteIconButton.builder(
                Component.translatable("config.snapper.customScreenshotFolder.reset"),
                _ -> {
                    functions.reset();
                    editBox.setValue(functions.get().toString());
                },
                true
        ).width(BUTTON_SIZE).sprite(RESET_ICON, 15, 15).build();
        this.resetButton.setTooltip(Tooltip.create(Component.translatable("config.snapper.customScreenshotFolder.reset")));

        layout.addChild(fileDialogButton);
        layout.addChild(resetButton);
        layout.addChild(editBox);

        this.layout.arrangeElements();
        this.setHeight(this.layout.getHeight());
    }

    public void setActive(boolean value) {
        this.active = value;

        editBox.setEditable(value);
        editBox.active = value;
        if (editBox.isFocused() && !value) {
            editBox.setFocused(false);
        }

        fileDialogButton.active = value;
        resetButton.active = value;
    }

    private void valueFromSelectDialog(@Nullable Path value) {
        if (value == null) {
            return;
        }

        if (Files.exists(value)) {
            this.functions.set(value);
            this.editBox.setValue(this.functions.get().toString());
        }
    }

    @Override
    public void setFocused(final @org.jspecify.annotations.Nullable GuiEventListener focused) {
        if (this.getFocused() != focused) {
            super.setFocused(focused);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    protected int contentHeight() {
        return this.height;
    }

    @Override
    public void setX(final int x) {
        super.setX(x);
        this.layout.setX(x);
        this.layout.arrangeElements();
    }

    @Override
    public void setY(final int y) {
        super.setY(y);
        this.layout.setY(y);
        this.layout.arrangeElements();
    }

    @Override
    public void setWidth(int width) {
        super.setWidth(width);
        this.editBox.setWidth(width - ((BUTTON_SIZE + PADDING) * 2));
        this.editBox.moveCursorToStart(false);
        this.layout.arrangeElements();
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        this.layout.visitWidgets(child -> child.extractRenderState(graphics, mouseX, mouseY, a));
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
    }

    @Override
    public boolean isMouseOver(final double mouseX, final double mouseY) {
        AtomicBoolean mouseOver = new AtomicBoolean();
        this.layout.visitChildren(child -> {
            if (child.getRectangle().containsPoint((int) mouseX, (int) mouseY)) {
                mouseOver.set(true);
            }
        });
        return mouseOver.get();
    }

    @Override
    public ScreenRectangle getRectangle() {
        return this.layout.getRectangle();
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return List.of(resetButton, fileDialogButton, editBox);
    }

    public static abstract class PathFunctions {
        public abstract Path get();

        public abstract void set(Path path);

        public abstract void reset();
    }
}
