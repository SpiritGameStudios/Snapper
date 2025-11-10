package dev.spiritstudios.snapper.gui.screen;

import dev.spiritstudios.snapper.util.ScreenshotActions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import java.nio.file.Path;

public class ScreenshotRenameScreen extends Screen {
    private final Path screenshot;
    private final TextFieldWidget renameInput;
    private final Text RENAME_INPUT_TEXT = Text.translatable("text.snapper.rename_input");
    private final MinecraftClient client = MinecraftClient.getInstance();
    private final TextRenderer textRenderer = client.textRenderer;
    private final Screen parent;

    protected ScreenshotRenameScreen(Path screenshot, Screen parent) {
        super(Text.translatable("text.snapper.rename"));
        this.screenshot = screenshot;
        this.renameInput = new TextFieldWidget(textRenderer, 200, 20, RENAME_INPUT_TEXT);
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.addDrawableChild(new TextWidget(RENAME_INPUT_TEXT, textRenderer))
                .setPosition(this.width / 2 - textRenderer.getWidth(RENAME_INPUT_TEXT) / 2, this.height / 2 - 20);

        this.addDrawableChild(this.renameInput).setPosition(this.width / 2 - 100, this.height / 2);

        this.renameInput.setText(this.screenshot.getFileName().toString());
        this.addDrawableChild(ButtonWidget.builder(
                Text.translatable("button.snapper.rename"),
                button -> this.renameScreenshot(this.renameInput.getText())
        ).dimensions(width / 2 - 150 - 4, height - 32, 150, 20).build());

        this.addDrawableChild(ButtonWidget.builder(
                ScreenTexts.CANCEL,
                button -> this.close()
        ).dimensions(width / 2 + 4, height - 32, 150, 20).build());
    }

    private void renameScreenshot(String newName) {
        if (newName == null || !newName.endsWith(".png")) return;

        ScreenshotActions.renameScreenshot(screenshot, newName);
        client.setScreen(this.parent);
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }
}
