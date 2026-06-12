package com.pieta.piscosmetics.client;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import com.pieta.piscosmetics.ModParticles;

public record CosmeticParticleOptions(
        ResourceLocation texture,
        float size,
        int lifetime
) implements ParticleOptions {

    public static final MapCodec<CosmeticParticleOptions> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("texture").forGetter(CosmeticParticleOptions::texture),
                    Codec.FLOAT.optionalFieldOf("size", 0.25f).forGetter(CosmeticParticleOptions::size),
                    Codec.INT.optionalFieldOf("lifetime", 30).forGetter(CosmeticParticleOptions::lifetime)
            ).apply(instance, CosmeticParticleOptions::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, CosmeticParticleOptions> STREAM_CODEC =
            StreamCodec.of(CosmeticParticleOptions::encode, CosmeticParticleOptions::decode);

    private static void encode(RegistryFriendlyByteBuf buf, CosmeticParticleOptions options) {
        buf.writeResourceLocation(options.texture);
        buf.writeFloat(options.size);
        buf.writeInt(options.lifetime);
    }

    private static CosmeticParticleOptions decode(RegistryFriendlyByteBuf buf) {
        return new CosmeticParticleOptions(
                buf.readResourceLocation(),
                buf.readFloat(),
                buf.readInt()
        );
    }

    @Override
    public ParticleType<?> getType() {
        return ModParticles.COSMETIC_PARTICLE.get();
    }
}