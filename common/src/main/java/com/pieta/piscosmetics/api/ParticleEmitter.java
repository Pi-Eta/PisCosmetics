package com.pieta.piscosmetics.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public record ParticleEmitter(
        String trigger,
        ResourceLocation particleId,
        int rate,
        double spread,
        int count,
        Optional<Double> offsetX,
        Optional<Double> offsetY,
        Optional<Double> offsetZ,
        Optional<String> color,
        Optional<ResourceLocation> texture,
        Optional<Float> customSize,
        Optional<Integer> lifetime
) {
    public static final Codec<ParticleEmitter> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("trigger").forGetter(ParticleEmitter::trigger),
                    ResourceLocation.CODEC.fieldOf("particle").forGetter(ParticleEmitter::particleId),
                    Codec.INT.fieldOf("rate").forGetter(ParticleEmitter::rate),
                    Codec.DOUBLE.optionalFieldOf("spread", 0.5).forGetter(ParticleEmitter::spread),
                    Codec.INT.optionalFieldOf("count", 1).forGetter(ParticleEmitter::count),
                    Codec.DOUBLE.optionalFieldOf("offset_x").forGetter(ParticleEmitter::offsetX),
                    Codec.DOUBLE.optionalFieldOf("offset_y").forGetter(ParticleEmitter::offsetY),
                    Codec.DOUBLE.optionalFieldOf("offset_z").forGetter(ParticleEmitter::offsetZ),
                    Codec.STRING.optionalFieldOf("color").forGetter(ParticleEmitter::color),
                    ResourceLocation.CODEC.optionalFieldOf("texture").forGetter(ParticleEmitter::texture),
                    Codec.FLOAT.optionalFieldOf("custom_size").forGetter(ParticleEmitter::customSize),
                    Codec.INT.optionalFieldOf("lifetime").forGetter(ParticleEmitter::lifetime)
            ).apply(instance, ParticleEmitter::new)
    );
}