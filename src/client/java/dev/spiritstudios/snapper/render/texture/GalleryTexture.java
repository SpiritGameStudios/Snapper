package dev.spiritstudios.snapper.render.texture;

import com.mojang.blaze3d.platform.NativeImage;
import dev.spiritstudios.snapper.Snapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public sealed abstract class GalleryTexture implements AutoCloseable permits PanoramaTexture, ScreenshotTexture {
    protected static final Identifier UNKNOWN_SERVER = Identifier.withDefaultNamespace("textures/misc/unknown_server.png");

    private static final int MAX_LOADING = 10;
    private static final AtomicInteger CURRENTLY_LOADING = new AtomicInteger();

    protected final TextureManager textureManager;
    public final Identifier textureLocation;
    public final Path path;

    protected boolean isClosed;
    protected boolean isLoaded;
    protected boolean didLoadFail;
    protected boolean isLoadingStarted;

    protected GalleryTexture(TextureManager textureManager, Identifier textureLocation, Path path) {
        this.textureManager = textureManager;
        this.textureLocation = textureLocation;
        this.path = path;
    }

    protected abstract NativeImage load();
    protected abstract void upload(NativeImage image);

    public synchronized void startLoading(Minecraft minecraft, boolean force) {
        if (!isLoaded && !didLoadFail && !isLoadingStarted && (CURRENTLY_LOADING.get() < MAX_LOADING || force)) {
            CURRENTLY_LOADING.incrementAndGet();
            this.isLoadingStarted = true;

            CompletableFuture
                    .supplyAsync(this::load, Util.nonCriticalIoPool())
                    .thenAcceptAsync(this::upload, minecraft)
                    .whenComplete((_, e) -> {
                        if (e != null) {
                            Snapper.LOGGER.error("Error loading image", e);
                            this.didLoadFail = true;
                        }

                        CURRENTLY_LOADING.decrementAndGet();
                    });
        }
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    public boolean didLoadFail() {
        return didLoadFail;
    }

    public abstract int getWidth();
    public abstract int getHeight();

    @Override
    public abstract void close();

    public abstract Screen createViewer(@Nullable Screen parent);
}
