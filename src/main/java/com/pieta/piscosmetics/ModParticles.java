package com.pieta.piscosmetics;

import com.mojang.serialization.MapCodec;
import com.pieta.piscosmetics.client.CosmeticParticle;
import com.pieta.piscosmetics.client.CosmeticParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, PisCosmetics.MODID);

    public static final Supplier<ParticleType<CosmeticParticleOptions>> COSMETIC_PARTICLE =
            PARTICLES.register("cosmetic_particle", () -> new ParticleType<CosmeticParticleOptions>(false) {
                @Override
                public MapCodec<CosmeticParticleOptions> codec() {
                    return CosmeticParticleOptions.CODEC;
                }
                @Override
                public StreamCodec<? super RegistryFriendlyByteBuf, CosmeticParticleOptions> streamCodec() {
                    return CosmeticParticleOptions.STREAM_CODEC;
                }
            });

    public static void registerProviders(RegisterParticleProvidersEvent event) {
        event.registerSpecial(COSMETIC_PARTICLE.get(), new CosmeticParticle.Provider());
    }
}