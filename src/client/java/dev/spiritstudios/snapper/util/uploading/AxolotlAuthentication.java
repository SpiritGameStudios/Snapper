package dev.spiritstudios.snapper.util.uploading;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;

import java.util.UUID;

public record AxolotlAuthentication(String username, UUID uuid, String accessToken) {
    public static final Codec<AxolotlAuthentication> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("username").forGetter(AxolotlAuthentication::username),
            UUIDUtil.LENIENT_CODEC.fieldOf("uuid").forGetter(AxolotlAuthentication::uuid),
            Codec.STRING.fieldOf("access_token").forGetter(AxolotlAuthentication::accessToken)
    ).apply(instance, AxolotlAuthentication::new));
}
