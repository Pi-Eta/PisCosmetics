package com.pieta.piscosmetics;

import com.mojang.serialization.MapCodec;
import com.pieta.piscosmetics.client.CosmeticParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiConsumer;

/**
 * Custom particle types for the two loaders
 */
public final class ModParticles {

    public static ParticleType<CosmeticParticleOptions> COSMETIC_PARTICLE;

    private ModParticles() {}

    public static void register(BiConsumer<ResourceLocation, ParticleType<?>> registrar) {
        COSMETIC_PARTICLE = new ParticleType<CosmeticParticleOptions>(false) {
            @Override
            public MapCodec<CosmeticParticleOptions> codec() {
                return CosmeticParticleOptions.CODEC;
            }

            @Override
            public StreamCodec<? super RegistryFriendlyByteBuf, CosmeticParticleOptions> streamCodec() {
                return CosmeticParticleOptions.STREAM_CODEC;
            }
        };
        registrar.accept(ResourceLocation.fromNamespaceAndPath(PisCosmetics.MODID, "cosmetic_particle"), COSMETIC_PARTICLE);
    }
}
