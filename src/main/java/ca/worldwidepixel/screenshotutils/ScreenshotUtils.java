package ca.worldwidepixel.screenshotutils;

import ca.worldwidepixel.screenshotutils.mixin.HugeScreenshotInvoker;
import ca.worldwidepixel.screenshotutils.screen.screenshot.ScreenshotScreen;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScreenshotUtils implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("screenshotutils");
	private static KeyBinding screenshotMenuKey;
	private static KeyBinding panoramaKey;
	private static KeyBinding hugeShotKey;

	@Override
	public void onInitialize() {
		screenshotMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.screenshotutils.screenshotMenu",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_M,
				"key.categories.misc"
		));
		panoramaKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.screenshotutils.panorama",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_F7,
				"key.categories.misc"
		));
		/*hugeShotKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.screenshotutils.huge",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_F8,
				"key.categories.misc"
		));*/

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (screenshotMenuKey.wasPressed()) {
				client.setScreen(new ScreenshotScreen());
			}
			while (panoramaKey.wasPressed()) {
				client.takePanorama(client.runDirectory, 1024, 1024);
			}
			/*while (hugeShotKey.wasPressed()) {
				((HugeScreenshotInvoker) client).callTakeHugeScreenshot(client.runDirectory, 32, 32, 1024, 768);
			}*/
		});

		LOGGER.info("ScreenshotUtils initialised");
	}
}