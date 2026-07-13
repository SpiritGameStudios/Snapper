package dev.spiritstudios.snapper.render;

import dev.spiritstudios.snapper.Snapper;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import java.util.function.Function;

public class SnapperRenderTypes {
    private static final Function<Identifier, RenderType> PANORAMA = Util.memoize(
            texture -> {
                RenderSetup state = RenderSetup.builder(RenderPipelines.PANORAMA)
                        .withTexture("Sampler0", texture)
                        .createRenderSetup();
                return RenderType.create(Snapper.MOD_ID + ":panorama", state);
            }
    );

    public static RenderType panorama(Identifier texture) {
        return PANORAMA.apply(texture);
    }
}
