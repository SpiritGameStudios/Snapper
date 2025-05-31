package dev.spiritstudios.snapper.gui.widget;

import dev.spiritstudios.snapper.util.SnapperUtil;
import dev.spiritstudios.snapper.util.config.DirectoryUtil;
import dev.spiritstudios.specter.api.config.Value;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ContainerWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.io.File;
import java.util.List;

import static dev.spiritstudios.snapper.Snapper.MODID;

public class FolderSelectWidget extends ContainerWidget {
    private final Value<File> value;
    private final String placeholder_key;
    private static final Identifier FOLDER_ICON = Identifier.of(MODID, "screenshots/folder");
    private static final Identifier RESET_ICON = Identifier.of(MODID, "screenshots/reset");

    public FolderSelectWidget(int x, int y, int width, int height, Value<File> value, String placeholder_key) {
        super(x, y, width, height, ScreenTexts.EMPTY);
        this.value = value;
        this.placeholder_key = placeholder_key;
    }

    @Override
    public List<? extends ClickableWidget> children() {
        TextFieldWidget textFieldWidget = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, getX() + 25 + 25, getY(), width - 25 - 25, height, Text.of(value.get().getPath()));
        textFieldWidget.setPlaceholder(Text.translatableWithFallback(placeholder_key, "").formatted(Formatting.DARK_GRAY));
        textFieldWidget.setMaxLength(Integer.MAX_VALUE);
        textFieldWidget.setText(value.get().getPath());
        textFieldWidget.setChangedListener(content -> value.set(new File(content)));

        TextIconButtonWidget folderSelectButton = TextIconButtonWidget.builder(
                Text.translatable("config.snapper.snapper.customScreenshotFolder.select"),
                button -> DirectoryUtil.openFolderSelect(Text.translatable("prompt.snapper.folder_select").getString()),
                true
        ).width(20).texture(FOLDER_ICON, 15, 15).build();
        folderSelectButton.setPosition(this.getX(), this.getY());

        TextIconButtonWidget resetFolderButton = TextIconButtonWidget.builder(
                Text.translatable("config.snapper.snapper.customScreenshotFolder.reset"),
                button -> value.set(new File(DirectoryUtil.escapePath(SnapperUtil.getOSUnifiedFolder().toString()))),
                true
        ).width(20).texture(RESET_ICON, 15, 15).build();
        resetFolderButton.setPosition(this.getX() + 25, this.getY());

        return List.of(
            textFieldWidget, folderSelectButton, resetFolderButton
        );
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        this.children().forEach(child -> {
            child.render(context, mouseX, mouseY, delta);
        });
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    }
}
