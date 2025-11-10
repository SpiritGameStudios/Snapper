package dev.spiritstudios.snapper.gui.widget;

import dev.spiritstudios.snapper.Snapper;
import dev.spiritstudios.snapper.gui.overlay.ExternalDialogOverlay;
import dev.spiritstudios.snapper.util.config.DirectoryConfigUtil;
import dev.spiritstudios.specter.api.config.Value;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ContainerWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

public class FolderSelectWidget extends ContainerWidget implements ParentElement {
	private static final Identifier FOLDER_ICON = Snapper.id("screenshots/folder");
	private static final Identifier RESET_ICON = Snapper.id("screenshots/reset");

	private static final int BUTTON_WIDTH = 25; // Includes padding

	private final Value<Path> value;

	private final TextFieldWidget textInput;
	private final TextIconButtonWidget fileDialogButton;
	private final TextIconButtonWidget resetButton;

	public FolderSelectWidget(int x, int y, int width, int height, Value<Path> value, String placeholderKey) {
		super(x, y, width, height, ScreenTexts.EMPTY);
		this.value = value;
		this.active = false;

		MinecraftClient client = MinecraftClient.getInstance();

		this.textInput = new TextFieldWidget(
				client.textRenderer,
				BUTTON_WIDTH * 2, 0,
				width - (BUTTON_WIDTH * 2),
				20,
				Text.of(value.get().toString())
		);

		this.textInput.setPlaceholder(Text.translatableWithFallback(placeholderKey, "").formatted(Formatting.DARK_GRAY));
		this.textInput.setMaxLength(4096); // Unix maximum path length, shorter on windows (I think it may have been 240)
		this.textInput.setText(value.get().toString());
		this.textInput.setChangedListener(content -> {
			Path path;

			try {
				path = Path.of(content);
			} catch (InvalidPathException ignored) {
				path = null;
			}

			if (path == null || !Files.exists(path) || !Files.isDirectory(path)) {
				this.textInput.setEditableColor(Colors.RED);
			} else {
				this.textInput.setEditableColor(Colors.WHITE);
				value.set(path);
			}
		});
		this.textInput.setTooltip(Tooltip.of(Text.translatable("config.snapper.snapper.customScreenshotFolder.input")));

		this.fileDialogButton = TextIconButtonWidget.builder(
						Text.translatable("config.snapper.snapper.customScreenshotFolder.select"),
						button -> {
							ExternalDialogOverlay overlay = new ExternalDialogOverlay();
							client.setOverlay(overlay);

							DirectoryConfigUtil.openFolderSelect(
											Text.translatable("prompt.snapper.folder_select")
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
				.texture(FOLDER_ICON, 15, 15)
				.build();
		this.fileDialogButton.setTooltip(Tooltip.of(Text.translatable("config.snapper.snapper.customScreenshotFolder.select")));

		this.resetButton = TextIconButtonWidget.builder(
				Text.translatable("config.snapper.snapper.customScreenshotFolder.reset"),
				button -> {
					value.reset();
					textInput.setText(value.get().toString());
				},
				true
		).width(20).texture(RESET_ICON, 15, 15).build();
		this.resetButton.setTooltip(Tooltip.of(Text.translatable("config.snapper.snapper.customScreenshotFolder.reset")));
		resetButton.setX(BUTTON_WIDTH);
	}

	@Override
	public List<? extends ClickableWidget> children() {
		return List.of(
				this.fileDialogButton, this.resetButton, this.textInput
		);
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
			this.textInput.setText(this.value.get().toString());
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		int clicksRan = 0;

		for (ClickableWidget child : this.children()) {
			if (child.isHovered()) {
				clicksRan += 1;
				this.playDownSound(MinecraftClient.getInstance().getSoundManager());

				if (child == textInput) {
					textInput.setFocused(true);
					this.setFocused(textInput);
					textInput.setFocusUnlocked(true);
				}

				child.onClick(mouseX, mouseY);
			}
		}

		return clicksRan == 1;
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		for (Drawable drawable : this.children()) {
			drawable.render(context, mouseX, mouseY, deltaTicks);
		}
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {
		fileDialogButton.appendClickableNarrations(builder);
		resetButton.appendClickableNarrations(builder);
		textInput.appendClickableNarrations(builder);
	}

	@Override
	public void forEachChild(Consumer<ClickableWidget> consumer) {
		this.children().forEach(consumer);
	}

	@Override
	protected int getContentsHeightWithPadding() {
		return 20;
	}

	@Override
	protected double getDeltaYPerScroll() {
		return 20 / 2f;
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		this.active = true;
		var hovered = super.isMouseOver(mouseX, mouseY);
		this.active = false;
		return hovered;
	}
}
