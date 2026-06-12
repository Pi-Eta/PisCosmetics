package com.pieta.piscosmetics.item;

import com.pieta.piscosmetics.api.CosmeticDefinition;
import com.pieta.piscosmetics.data.CosmeticDataLoader;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public class CosmeticHelper {

    public static CosmeticDefinition getDefinition(ItemStack stack) {
        if (stack == null) return null;
        String cosmeticId = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                .copyTag().getString("cosmetic");
        if (cosmeticId.isEmpty()) return null;
        ResourceLocation id = ResourceLocation.tryParse(cosmeticId);
        if (id == null) return null;
        return CosmeticDataLoader.getDefinition(id);
    }

    public static ResourceLocation getModelResource(Item animatable, CosmeticDefinition def, ResourceLocation fallback) {
        if (def != null && def.model().isPresent()) {
            ResourceLocation model = def.model().get();
            String path = model.getPath();
            if (!path.endsWith(".geo.json")) path += ".geo.json";
            if (!path.startsWith("geo/")) path = "geo/" + path;
            return ResourceLocation.fromNamespaceAndPath(model.getNamespace(), path);
        }
        return fallback;
    }

    public static ResourceLocation getTextureResource(Item animatable, CosmeticDefinition def, ResourceLocation fallback) {
        if (def != null && def.texture().isPresent()) {
            ResourceLocation texture = def.texture().get();
            String path = texture.getPath();
            if (!path.endsWith(".png")) path += ".png";
            if (!path.startsWith("textures/")) path = "textures/" + path;
            return ResourceLocation.fromNamespaceAndPath(texture.getNamespace(), path);
        }
        return fallback;
    }

    public static ResourceLocation getAnimationResource(Item animatable, CosmeticDefinition def, ResourceLocation fallback) {
        if (def != null && def.animation().isPresent()) {
            String anim = def.animation().get();
            ResourceLocation animId = ResourceLocation.tryParse(anim);
            if (animId != null) {
                System.out.println("ANIM PATH = " + "animations/" + animId.getPath() + ".animation.json");
                return ResourceLocation.fromNamespaceAndPath(animId.getNamespace(),
                        "animations/" + animId.getPath() + ".animation.json");
            }
        }
        // Fallback to animations/ instead of animations/item/
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(animatable);
        return ResourceLocation.fromNamespaceAndPath(
                key.getNamespace(),
                "animations/" + key.getPath() + ".animation.json"
        );
    }

    public static net.minecraft.network.chat.Component getName(ItemStack stack, net.minecraft.network.chat.Component fallback) {
        CosmeticDefinition def = getDefinition(stack);
        if (def != null && def.name().isPresent()) {
            return net.minecraft.network.chat.Component.literal(def.name().get());
        }
        return fallback;
    }
}