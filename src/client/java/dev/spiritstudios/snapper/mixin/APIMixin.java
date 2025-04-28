package dev.spiritstudios.snapper.mixin;

import java.net.http.HttpClient;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.spiritstudios.snapper.util.uploading.ApiSupplier;
import dev.spiritstudios.snapper.util.uploading.ScreenshotUploading;
import io.github.axolotlclient.api.API;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = API.class, remap = false)
public class APIMixin {

	/*
	 * Add a bit of information to AxolotlClient's user-agent that this is Snapper and not AxolotlClient itself.
	 */
	@WrapOperation(method = "lambda$request$8", at = @At(value = "INVOKE", target = "Lio/github/axolotlclient/util/NetworkUtil;createHttpClient(Ljava/lang/String;)Ljava/net/http/HttpClient;"))
	private HttpClient changeUserAgent(String id, Operation<HttpClient> original) {
		var self = (API) (Object) this;
		if (self.getClass() == ApiSupplier.ApiExtension.class) {
			id += "/Snapper " + ScreenshotUploading.SNAPPER_VERSION;
		}
		return original.call(id);
	}
}
