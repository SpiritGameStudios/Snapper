package dev.spiritstudios.snapper.gui.widget;

import dev.spiritstudios.snapper.util.SnapperUtil;
import dev.spiritstudios.snapper.util.config.DirectoryUtil;
import dev.spiritstudios.specter.api.config.Value;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.*;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static dev.spiritstudios.snapper.Snapper.LOGGER;
import static dev.spiritstudios.snapper.Snapper.MODID;

public class FolderSelectWidget extends ContainerWidget implements ParentElement {
    private final Value<File> value;
    private final String placeholder_key;
    private static final Identifier FOLDER_ICON = Identifier.of(MODID, "screenshots/folder");
    private static final Identifier RESET_ICON = Identifier.of(MODID, "screenshots/reset");

    private final TextFieldWidget textFieldWidget;
    private final TextIconButtonWidget folderSelectButton;
    private final TextIconButtonWidget resetFolderButton;

    public FolderSelectWidget(int x, int y, int width, int height, Value<File> value, String placeholder_key) {
        super(x, y, width, height, ScreenTexts.EMPTY);
        this.value = value;
        this.placeholder_key = placeholder_key;
        this.active = false;

        this.textFieldWidget = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, 25 + 25, 0, width - 25 - 25, height, Text.of(value.get().getPath()));
        this.textFieldWidget.setPlaceholder(Text.translatableWithFallback(placeholder_key, "").formatted(Formatting.DARK_GRAY));
        this.textFieldWidget.setMaxLength(Integer.MAX_VALUE);
        this.textFieldWidget.setText(value.get().getPath());
        this.textFieldWidget.setChangedListener(content -> {
            value.set(new File(content));
        });

        this.folderSelectButton = TextIconButtonWidget.builder(
                Text.translatable("config.snapper.snapper.customScreenshotFolder.select"),
                button -> {
                    Optional<File> folderValue = DirectoryUtil.openFolderSelect(Text.translatable("prompt.snapper.folder_select").getString());
                    valueFromSelectDialog(folderValue.orElse(null));
                },
                true
        ).width(20).texture(FOLDER_ICON, 15, 15).build();
        //this.folderSelectButton.setPosition(this.getX(), this.getY());

        this.resetFolderButton = TextIconButtonWidget.builder(
                Text.translatable("config.snapper.snapper.customScreenshotFolder.reset"),
                button -> value.set(new File(DirectoryUtil.escapePath(SnapperUtil.getOSUnifiedFolder().toString()))),
                true
        ).width(20).texture(RESET_ICON, 15, 15).build();
        resetFolderButton.setX(25);
        //resetFolderButton.setPosition(this.getX() + 25, this.getY());
    }

    @Override
    public List<? extends ClickableWidget> children() {
        return List.of(
                this.folderSelectButton, this.resetFolderButton, this.textFieldWidget
        );
    }

    private void valueFromSelectDialog(@Nullable File value) {
        if (value == null) {
            return;
        }
        if (value.exists()) {
            this.value.set(value);
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        super.onClick(mouseX, mouseY);
        this.children().forEach(child -> {
            if (SnapperUtil.inBoundingBox(child.getX(), child.getY(), child.getWidth(), child.getHeight(), mouseX, mouseY)) {
                child.onClick(mouseX, mouseY);
            }
        });
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        // Special thanks to Falkreon for this workaround
        this.children().forEach(child -> {
            context.getMatrices().push();
            context.getMatrices().translate(this.getX(), this.getY(), 0);
            child.render(context, mouseX, mouseY, delta);
            context.getMatrices().pop();
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
