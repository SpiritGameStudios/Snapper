package dev.spiritstudios.snapper.gui.screen;

import dev.spiritstudios.snapper.util.ScreenshotActions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.nio.file.Path;

public class ScreenshotRenameScreen extends Screen {
    private final Path screenshot;
    private final EditBox renameInput;
    private final Component RENAME_INPUT_TEXT = Component.translatable("text.snapper.rename_input");
    private final Minecraft client = Minecraft.getInstance();
    private final Font textRenderer = client.font;
    private final Screen parent;

    protected ScreenshotRenameScreen(Path screenshot, Screen parent) {
        super(Component.translatable("text.snapper.rename"));
        this.screenshot = screenshot;
        this.renameInput = new EditBox(textRenderer, 200, 20, RENAME_INPUT_TEXT);
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.addRenderableWidget(new StringWidget(RENAME_INPUT_TEXT, textRenderer))
                .setPosition(this.width / 2 - textRenderer.width(RENAME_INPUT_TEXT) / 2, this.height / 2 - 20);

        this.addRenderableWidget(this.renameInput).setPosition(this.width / 2 - 100, this.height / 2);

        this.renameInput.setValue(this.screenshot.getFileName().toString());
        this.renameInput.setMaxLength(255);
        this.addRenderableWidget(Button.builder(
                Component.translatable("button.snapper.rename"),
                button -> this.renameScreenshot(this.renameInput.getValue())
        ).bounds(width / 2 - 150 - 4, height - 32, 150, 20).build());

        this.addRenderableWidget(Button.builder(
                CommonComponents.GUI_CANCEL,
                button -> this.onClose()
        ).bounds(width / 2 + 4, height - 32, 150, 20).build());
    }

    private void renameScreenshot(String newName) {
        if (newName == null || !newName.endsWith(".png")) return;

        ScreenshotActions.renameScreenshot(screenshot, newName);
        client.setScreen(this.parent);
    }

    @Override
    public void onClose() {
        this.client.setScreen(this.parent);
    }
}
