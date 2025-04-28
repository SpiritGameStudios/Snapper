package dev.spiritstudios.snapper.util.uploading;

import java.util.Collection;
import java.util.List;

import dev.spiritstudios.snapper.Snapper;
import dev.spiritstudios.snapper.SnapperConfig;
import io.github.axolotlclient.AxolotlClientCommon;
import io.github.axolotlclient.AxolotlClientConfig.api.manager.ConfigManager;
import io.github.axolotlclient.AxolotlClientConfig.api.options.OptionCategory;
import io.github.axolotlclient.api.API;
import io.github.axolotlclient.api.Options;
import io.github.axolotlclient.api.Request;
import io.github.axolotlclient.api.util.StatusUpdateProvider;
import io.github.axolotlclient.util.Logger;
import io.github.axolotlclient.util.translation.TranslationProvider;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;

public class ApiSupplier {

	public static void loadAPI() {
		if (!ScreenshotUploading.AXOLOTLCLIENT_LOADED) {
			if (AxolotlClientCommon.getInstance() == null) {
				new AxolotlClientCommon(new LoggerImpl(),
						(s, s1, objects) -> MinecraftClient.getInstance().getToastManager().add(
								SystemToast.create(MinecraftClient.getInstance(),
										SystemToast.Type.WORLD_BACKUP,
										Text.translatable(s, objects),
										Text.translatable(s1, objects))),
						DummyConfigManager::new);
			}
			if (API.getInstance() == null) {
				new ApiExtension(new LoggerImpl(), I18n::translate, StatusProviderImpl.INSTANCE, new ApiOptionsImpl());
			}
			API.getInstance().getApiOptions().privacyAccepted.set(SnapperConfig.INSTANCE.termsAccepted.get());
		}
	}

	public static class ApiExtension extends API {

		private ApiExtension(Logger logger, TranslationProvider translationProvider, StatusUpdateProvider statusUpdateProvider, Options apiOptions) {
			super(logger, translationProvider, statusUpdateProvider, apiOptions);
		}
	}

	private static class DummyConfigManager implements ConfigManager {

		private static final OptionCategory root = OptionCategory.create("root");

		@Override
		public void save() {
			SnapperConfig.INSTANCE.termsAccepted.set(API.getInstance().getApiOptions().privacyAccepted.get());
			SnapperConfig.HOLDER.save();
		}

		@Override
		public void load() {

		}

		@Override
		public OptionCategory getRoot() {
			return root;
		}

		@Override
		public Collection<String> getSuppressedNames() {
			return List.of();
		}

		@Override
		public void suppressName(String name) {

		}
	}

	private static class ApiOptionsImpl extends Options {

		public ApiOptionsImpl() {
			init();
			category.add(privacyAccepted);

			var client = MinecraftClient.getInstance();
			openPrivacyNoteScreen = bool -> client.execute(() -> client.setScreen(new PrivacyNoticeScreen(client.currentScreen, bool)));
		}
	}

	private static class StatusProviderImpl implements StatusUpdateProvider {

		public static final StatusUpdateProvider INSTANCE = new StatusProviderImpl();

		@Override
		public void initialize() {

		}

		@Override
		public Request getStatus() {
			return null;
		}
	}


	private static class LoggerImpl implements Logger {

		@Override
		public void info(String s, Object... objects) {

		}

		@Override
		public void warn(String s, Object... objects) {
			Snapper.LOGGER.warn(s, objects);
		}

		@Override
		public void error(String s, Object... objects) {
			Snapper.LOGGER.error(s, objects);
		}

		@Override
		public void debug(String s, Object... objects) {

		}
	}
}
