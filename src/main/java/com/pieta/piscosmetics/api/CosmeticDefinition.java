package com.pieta.piscosmetics.api;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
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
        List<ParticleEmitter> particleEmitters,
        List<String> hideArmor,
        Optional<ResourceLocation> icon
) {
    public static final Codec<CosmeticDefinition> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("slot").forGetter(CosmeticDefinition::slot),
                    ResourceLocation.CODEC.optionalFieldOf("model").forGetter(CosmeticDefinition::model),
                    ResourceLocation.CODEC.optionalFieldOf("texture").forGetter(CosmeticDefinition::texture),
                    Codec.STRING.optionalFieldOf("animation").forGetter(CosmeticDefinition::animation),
                    Codec.STRING.optionalFieldOf("name").forGetter(CosmeticDefinition::name),
                    Codec.STRING.optionalFieldOf("attach").forGetter(CosmeticDefinition::attach),
                    Codec.FLOAT.optionalFieldOf("translate_x").forGetter(CosmeticDefinition::translateX),
                    Codec.FLOAT.optionalFieldOf("translate_y").forGetter(CosmeticDefinition::translateY),
                    Codec.FLOAT.optionalFieldOf("translate_z").forGetter(CosmeticDefinition::translateZ),
                    Codec.FLOAT.optionalFieldOf("rotate_x").forGetter(CosmeticDefinition::rotateX),
                    Codec.FLOAT.optionalFieldOf("rotate_y").forGetter(CosmeticDefinition::rotateY),
                    Codec.FLOAT.optionalFieldOf("rotate_z").forGetter(CosmeticDefinition::rotateZ),
                    Codec.FLOAT.optionalFieldOf("scale").forGetter(CosmeticDefinition::scale),
                    Codec.list(ParticleEmitter.CODEC).optionalFieldOf("particle_emitters", List.of()).forGetter(CosmeticDefinition::particleEmitters),
                    Codec.either(Codec.STRING, Codec.STRING.listOf())
                            .xmap(either -> either.map(List::of, list -> list),
                                    list -> list.size() == 1 ? Either.left(list.get(0)) : Either.right(list))
                            .optionalFieldOf("hide_armor", List.of())
                            .forGetter(CosmeticDefinition::hideArmor),
                    ResourceLocation.CODEC.optionalFieldOf("icon").forGetter(CosmeticDefinition::icon)
            ).apply(instance, CosmeticDefinition::new)
    );

    public String getAttachPoint() {
        return attach.orElse("body");
    }
}