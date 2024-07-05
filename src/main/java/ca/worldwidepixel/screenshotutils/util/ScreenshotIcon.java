package ca.worldwidepixel.screenshotutils.util;

import com.google.common.hash.Hashing;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class ScreenshotIcon implements AutoCloseable {
    private static final Identifier UNKNOWN_SERVER_ID = Identifier.ofVanilla("textures/misc/unknown_server.png");
    private static final int ICON_WIDTH = 64;
    private static final int ICON_HEIGHT = 64;
    private final TextureManager textureManager;
    private final Identifier id;
    @Nullable
    private NativeImageBackedTexture texture;
    private boolean closed;

    private ScreenshotIcon(TextureManager textureManager, Identifier id) {
        this.textureManager = textureManager;
        this.id = id;
    }

    public static ScreenshotIcon forScreenshot(TextureManager textureManager, String screenshotName) {
        return new ScreenshotIcon(
                textureManager,
                Identifier.ofVanilla(
                        "screenshots/" + Util.replaceInvalidChars(screenshotName, Identifier::isPathCharacterValid) + "/" + Hashing.sha1().hashUnencodedChars(screenshotName) + "/icon"
                )
        );
    }

    public void load(NativeImage image) {
        //if (image.getWidth() == 64 && image.getHeight() == 64) {
        if (true) {
            try {
                this.assertOpen();
                if (this.texture == null) {
                    this.texture = new NativeImageBackedTexture(image);
                } else {
                    this.texture.setImage(image);
                    this.texture.upload();
                }

                this.textureManager.registerTexture(this.id, this.texture);
            } catch (Throwable var3) {
                image.close();
                this.destroy();
                throw var3;
            }
        } else {
            image.close();
            throw new IllegalArgumentException("Icon must be 64x64, but was " + image.getWidth() + "x" + image.getHeight());
        }
    }

    public void destroy() {
        this.assertOpen();
        if (this.texture != null) {
            this.textureManager.destroyTexture(this.id);
            this.texture.close();
            this.texture = null;
        }
    }

    public Identifier getTextureId() {
        return this.texture != null ? this.id : UNKNOWN_SERVER_ID;
    }

    public void close() {
        this.destroy();
        this.closed = true;
    }

    private void assertOpen() {
        if (this.closed) {
            throw new IllegalStateException("Icon already closed");
        }
    }
}
