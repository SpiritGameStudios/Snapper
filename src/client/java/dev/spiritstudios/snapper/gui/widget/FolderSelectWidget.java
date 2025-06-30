package dev.spiritstudios.snapper.gui.widget;

import dev.spiritstudios.snapper.util.SnapperUtil;
import dev.spiritstudios.snapper.util.config.DirectoryConfigUtil;
import dev.spiritstudios.specter.api.config.Value;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.*;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static dev.spiritstudios.snapper.Snapper.MODID;

public class FolderSelectWidget extends ContainerWidget implements ParentElement {
    private final Value<Path> value;
    private static final Identifier FOLDER_ICON = Identifier.of(MODID, "screenshots/folder");
    private static final Identifier RESET_ICON = Identifier.of(MODID, "screenshots/reset");

    private final TextFieldWidget textField;
    private final TextIconButtonWidget folderSelectButton;
    private final TextIconButtonWidget resetFolderButton;

    /*
        Because of the visual bar at the top of config screens, this offset needs to exist for the mouse to notice the elements.
        There is probably a proper way to do this, but I cannot be bothered.
        Spectre Config upgrades should make this unnecessary.
        - WWP
    */
    private static final int WEIRD_FIX_OFFSET = 40;

    public FolderSelectWidget(int x, int y, int width, int height, Value<Path> value, String placeholderKey) {
        super(x, y, width, height, ScreenTexts.EMPTY);
        this.value = value;
        this.active = false;

        this.textField = new TextFieldWidget(
                MinecraftClient.getInstance().textRenderer,
                25 + 25, 0,
                120 - 25 - 25,
                20,
                Text.of(value.get().toString())
        );

        this.textField.setPlaceholder(Text.translatableWithFallback(placeholderKey, "").formatted(Formatting.DARK_GRAY));
        this.textField.setMaxLength(Integer.MAX_VALUE);
        this.textField.setText(value.get().toString());
        this.textField.setChangedListener(content -> value.set(Path.of(content)));
        this.textField.setTooltip(Tooltip.of(Text.translatable("config.snapper.snapper.customScreenshotFolder.input")));

        this.folderSelectButton = TextIconButtonWidget.builder(
                Text.translatable("config.snapper.snapper.customScreenshotFolder.select"),
                button -> {
                    Optional<Path> folderValue = DirectoryConfigUtil.openFolderSelect(Text.translatable("prompt.snapper.folder_select").getString());
                    valueFromSelectDialog(folderValue.orElse(null));
                },
                true
        ).width(20).texture(FOLDER_ICON, 15, 15).build();
        this.folderSelectButton.setTooltip(Tooltip.of(Text.translatable("config.snapper.snapper.customScreenshotFolder.select")));

        this.resetFolderButton = TextIconButtonWidget.builder(
                Text.translatable("config.snapper.snapper.customScreenshotFolder.reset"),
                button -> {
                    value.reset();
                    textField.setText(value.get().toString());
                },
                true
        ).width(20).texture(RESET_ICON, 15, 15).build();
        this.resetFolderButton.setTooltip(Tooltip.of(Text.translatable("config.snapper.snapper.customScreenshotFolder.reset")));
        resetFolderButton.setX(25);
    }

    @Override
    public List<? extends ClickableWidget> children() {
        List<ClickableWidget> children = List.of(
                this.folderSelectButton, this.resetFolderButton, this.textField
        );

        children.forEach(c -> c.setY(WEIRD_FIX_OFFSET));

        return children;
    }

    private void valueFromSelectDialog(@Nullable Path value) {
        if (value == null) {
            return;
        }
        if (Files.exists(value)) {
            this.value.set(value);
            this.textField.setText(this.value.get().toString());
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int clicksRan = 0;

        for (ClickableWidget child : this.children()) {
            if (child.isHovered()) {
                clicksRan += 1;
                this.playDownSound(MinecraftClient.getInstance().getSoundManager());
                if (child instanceof TextFieldWidget textFieldWidget) {
                    child.setFocused(true);
                    this.setFocused(child);
                    textFieldWidget.setFocusUnlocked(true);
                }
                child.onClick(mouseX - this.getX(), mouseY - this.getY() + WEIRD_FIX_OFFSET);
            }
        }

        return clicksRan == 1;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        // Jank is unavoidable sometimes
        if (this.width != textField.getWidth() + 25 + 25) {
            textField.setWidth(this.width - 25 - 25);
            int originalCursor = textField.getCursor();
            textField.setCursorToStart(false);
            textField.setCursor(originalCursor, false);
        }

        // Special thanks to Falkreon for this workaround
        this.children().forEach(child -> {
            context.getMatrices().push();
            {
                context.getMatrices().translate(this.getX(), this.getY() - WEIRD_FIX_OFFSET, 0);
                child.render(
                        context,
                        mouseX - this.getX(), mouseY - this.getY() + WEIRD_FIX_OFFSET,
                        delta
                );
            }
            context.getMatrices().pop();

            if (child instanceof TextFieldWidget textFieldWidget) {
                if (!Files.exists(Path.of(textFieldWidget.getText()))) {
                    textFieldWidget.setEditableColor(0xFF0000);
                    return;
                }

                textFieldWidget.setEditableColor(0xFFFFFF);
            }
        });
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    }

    @Override
    public void forEachChild(Consumer<ClickableWidget> consumer) {
        this.children().forEach(consumer);
    }
}
