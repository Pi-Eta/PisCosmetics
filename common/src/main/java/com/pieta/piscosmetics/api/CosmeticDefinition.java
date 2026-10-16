package com.pieta.piscosmetics.api;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.TextColor;
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
        Optional<ResourceLocation> icon,
        List<ResourceLocation> renderFor,
        List<String> keyItems,
        boolean copyable,
        Optional<TextColor> color
) {
    // One string or a list
    private static Codec<List<String>> stringOrList(Codec<String> element) {
        return Codec.either(element, element.listOf())
                .xmap(either -> either.map(List::of, list -> list),
                        list -> list.size() == 1 ? Either.left(list.get(0)) : Either.right(list));
    }

    /** First 16 fields, the codec builder's limit. The rest are added in {@link #CODEC} */
    private static final MapCodec<CosmeticDefinition> BASE_CODEC = RecordCodecBuilder.mapCodec(instance ->
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
                    stringOrList(Codec.STRING).optionalFieldOf("hide_armor", List.of())
                            .forGetter(CosmeticDefinition::hideArmor),
                    ResourceLocation.CODEC.optionalFieldOf("icon").forGetter(CosmeticDefinition::icon)
            ).apply(instance, (slot, model, texture, animation, name, attach, tx, ty, tz, rx, ry, rz, scale,
                               emitters, hideArmor, icon) -> new CosmeticDefinition(slot, model, texture, animation,
                    name, attach, tx, ty, tz, rx, ry, rz, scale, emitters, hideArmor, icon, List.of(), List.of(), true, Optional.empty()))
    );

    public static final Codec<CosmeticDefinition> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BASE_CODEC.forGetter(def -> def),
                    // Items this is the default worn look for
                    ResourceLocation.CODEC.listOf().optionalFieldOf("render_for", List.of()).forGetter(CosmeticDefinition::renderFor),
                    // Key item types the blueprint goes on (KeyItemTypes). Typos are rejected in CosmeticDataLoader
                    stringOrList(Codec.STRING)
                            .optionalFieldOf("key_items", List.of()).forGetter(CosmeticDefinition::keyItems),
                    // false: blueprints can't be copied
                    Codec.BOOL.optionalFieldOf("copyable", true).forGetter(CosmeticDefinition::copyable),
                    // Name colour, e.g. "gold" or "#FF8800"
                    TextColor.CODEC.optionalFieldOf("color").forGetter(CosmeticDefinition::color)
            ).apply(instance, (base, renderFor, keyItems, copyable, color) -> base.withExtras(renderFor, keyItems, copyable, color))
    );

    public CosmeticDefinition withExtras(List<ResourceLocation> renderFor, List<String> keyItems, boolean copyable,
                                         Optional<TextColor> color) {
        return new CosmeticDefinition(slot, model, texture, animation, name, attach, translateX, translateY, translateZ,
                rotateX, rotateY, rotateZ, scale, particleEmitters, hideArmor, icon, List.copyOf(renderFor),
                List.copyOf(keyItems), copyable, color);
    }

    /** True if the blueprint goes on key items instead of making a wearable cosmetic */
    public boolean isKeyItemLook() {
        return !keyItems.isEmpty();
    }

    public String getAttachPoint() {
        return attach.orElse("body");
    }
}