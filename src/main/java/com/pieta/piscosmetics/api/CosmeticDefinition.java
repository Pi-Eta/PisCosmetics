package com.pieta.piscosmetics.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public record CosmeticDefinition(
        String slot,
        Optional<ResourceLocation> model,
        Optional<ResourceLocation> texture,
        Optional<String> animation,
        Optional<String> name,
        Optional<String> attach,
        Optional<Float> translateX,
        Optional<Float> translateY,
        Optional<Float> translateZ,
        Optional<Float> rotateX,
        Optional<Float> rotateY,
        Optional<Float> rotateZ,
        Optional<Float> scale,
        Optional<ParticleSettings> particles,
        Optional<Boolean> elytra
) {
    public static final Codec<CosmeticDefinition> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("slot").forGetter(CosmeticDefinition::slot),
                    ResourceLocation.CODEC.optionalFieldOf("model").forGetter(CosmeticDefinition::model),
                    ResourceLocation.CODEC.optionalFieldOf("texture").forGetter(CosmeticDefinition::texture),
                    Codec.STRING.optionalFieldOf("animation").forGetter(CosmeticDefinition::animation),
                    Codec.STRING.optionalFieldOf("name").forGetter(CosmeticDefinition::name),
                    Codec.STRING.optionalFieldOf("attach").forGetter(CosmeticDefinition::attach),  // NEW
                    Codec.FLOAT.optionalFieldOf("translate_x").forGetter(CosmeticDefinition::translateX),
                    Codec.FLOAT.optionalFieldOf("translate_y").forGetter(CosmeticDefinition::translateY),
                    Codec.FLOAT.optionalFieldOf("translate_z").forGetter(CosmeticDefinition::translateZ),
                    Codec.FLOAT.optionalFieldOf("rotate_x").forGetter(CosmeticDefinition::rotateX),
                    Codec.FLOAT.optionalFieldOf("rotate_y").forGetter(CosmeticDefinition::rotateY),
                    Codec.FLOAT.optionalFieldOf("rotate_z").forGetter(CosmeticDefinition::rotateZ),
                    Codec.FLOAT.optionalFieldOf("scale").forGetter(CosmeticDefinition::scale),
                    ParticleSettings.CODEC.optionalFieldOf("particles").forGetter(CosmeticDefinition::particles),
                    Codec.BOOL.optionalFieldOf("elytra").forGetter(CosmeticDefinition::elytra)
            ).apply(instance, CosmeticDefinition::new)
    );

    public String getAttachPoint() {
        return attach.orElse("body");  // Default to body attach point if not specified
    }

    public boolean shouldDisableElytra() {
        return elytra.orElse(false);
    }

    public record ParticleSettings(
            ResourceLocation particleId,
            int rate,
            double spread,
            Optional<Float> offsetX,
            Optional<Float> offsetY,
            Optional<Float> offsetZ,
            Optional<String> color  // NEW - hex color like "#FF0000" for red
    ) {
        public static final Codec<ParticleSettings> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        ResourceLocation.CODEC.fieldOf("type").forGetter(ParticleSettings::particleId),
                        Codec.INT.optionalFieldOf("rate", 10).forGetter(ParticleSettings::rate),
                        Codec.DOUBLE.optionalFieldOf("spread", 0.5).forGetter(ParticleSettings::spread),
                        Codec.FLOAT.optionalFieldOf("offset_x").forGetter(ParticleSettings::offsetX),
                        Codec.FLOAT.optionalFieldOf("offset_y").forGetter(ParticleSettings::offsetY),
                        Codec.FLOAT.optionalFieldOf("offset_z").forGetter(ParticleSettings::offsetZ),
                        Codec.STRING.optionalFieldOf("color").forGetter(ParticleSettings::color)
                ).apply(instance, ParticleSettings::new)
        );
    }
}