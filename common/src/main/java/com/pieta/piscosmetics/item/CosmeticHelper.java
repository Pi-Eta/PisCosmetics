package com.pieta.piscosmetics.item;

import com.pieta.piscosmetics.ModItems;
import com.pieta.piscosmetics.api.CosmeticDefinition;
import com.pieta.piscosmetics.api.CosmeticSlotNames;
import com.pieta.piscosmetics.data.CosmeticDataLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.HashMap;
import java.util.Map;

public class CosmeticHelper {

    /** Where Polymer keeps the real item inside a disguised one */
    private static final String POLYMER_STACK = "$polymer:stack";

    /** Cached universal items for drawing stand-ins */
    private static final Map<ResourceLocation, ItemStack> DISPLAY_STACKS = new HashMap<>();

    public static CosmeticDefinition getDefinition(ItemStack stack) {
        ResourceLocation id = getCosmeticId(stack);
        return id == null ? null : CosmeticDataLoader.getDefinition(id);
    }

    /**
     * Cosmetic this stack shows, or null: its own "cosmetic" data, the same inside a Polymer disguise,
     * then render_for. Same result on server and client
     */
    public static ResourceLocation getCosmeticId(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        // Not copied, runs every frame
        CompoundTag data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).getUnsafe();
        CompoundTag hidden = data.getCompound(POLYMER_STACK);

        String cosmeticId = data.getString("cosmetic");
        if (cosmeticId.isEmpty()) {
            cosmeticId = hidden.getCompound("components").getCompound("minecraft:custom_data").getString("cosmetic");
        }
        if (!cosmeticId.isEmpty()) return ResourceLocation.tryParse(cosmeticId);

        String hiddenItemId = hidden.getString("id");
        ResourceLocation itemId = !hiddenItemId.isEmpty()
                ? ResourceLocation.tryParse(hiddenItemId)               // client, disguised
                : BuiltInRegistries.ITEM.getKey(stack.getItem());
        return CosmeticDataLoader.getStandIn(itemId);
    }

    /** Cosmetic to draw in place of another mod's item, or null */
    public static ResourceLocation getStandInId(ItemStack stack) {
        if (stack.getItem() instanceof CosmeticItem) return null;
        ResourceLocation id = getCosmeticId(stack);
        // Missing cosmetic: the item renders as normal
        return id != null && CosmeticDataLoader.getDefinition(id) != null ? id : null;
    }

    /** Look applied to another mod's item, or null. Ignores our items and render_for */
    public static ResourceLocation getAppliedLook(ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() instanceof CosmeticItem || stack.getItem() instanceof CosmeticBlueprintItem) return null;
        CompoundTag data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).getUnsafe();
        String cosmeticId = data.getString("cosmetic");
        if (cosmeticId.isEmpty()) {
            cosmeticId = data.getCompound(POLYMER_STACK).getCompound("components").getCompound("minecraft:custom_data").getString("cosmetic");
        }
        return cosmeticId.isEmpty() ? null : ResourceLocation.tryParse(cosmeticId);
    }

    /** Copy without the applied look, for the grindstone */
    public static ItemStack withoutLook(ItemStack stack) {
        ItemStack result = stack.copy();
        CustomData.update(DataComponents.CUSTOM_DATA, result, tag -> {
            tag.remove("cosmetic");
            // Client preview of a disguised item
            if (tag.contains(POLYMER_STACK)) {
                tag.getCompound(POLYMER_STACK).getCompound("components").getCompound("minecraft:custom_data").remove("cosmetic");
            }
        });
        return result;
    }

    /** True if Polymer disguised it as a vanilla item */
    public static boolean isDisguised(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).contains(POLYMER_STACK);
    }

    /** Real item ID inside a Polymer disguise, or null */
    public static ResourceLocation getDisguisedItemId(ItemStack stack) {
        if (!isDisguised(stack)) return null;
        String id = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).getUnsafe()
                .getCompound(POLYMER_STACK).getString("id");
        return id.isEmpty() ? null : ResourceLocation.tryParse(id);
    }

    /** Universal item showing this cosmetic, for stand-ins */
    public static ItemStack displayStack(ResourceLocation cosmeticId) {
        return DISPLAY_STACKS.computeIfAbsent(cosmeticId, id -> {
            ItemStack stack = new ItemStack(ModItems.COSMETIC);
            CompoundTag tag = new CompoundTag();
            tag.putString("cosmetic", id.toString());
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            return stack;
        });
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
                return ResourceLocation.fromNamespaceAndPath(animId.getNamespace(),
                        "animations/" + animId.getPath() + ".animation.json");
            }
        }
        // The renderer passes animations/<item>.animation.json (animations/, not animations/item/)
        return fallback;
    }

    /** True for cosmetics that go in a vanilla armor slot, which work without Accessories. */
    public static boolean canBeWornWithoutAccessories(ItemStack stack) {
        return CosmeticSlotNames.armorSlot(CosmeticSlotNames.slotKey(stack)) != null;
    }

    public static Component getName(ItemStack stack, Component fallback) {
        ResourceLocation id = getCosmeticId(stack);
        CosmeticDefinition def = getDefinition(stack);
        return def != null && def.name().isPresent() ? displayName(id) : fallback;
    }

    /** Used when the JSON has no "color" */
    public static final TextColor DEFAULT_NAME_COLOR = TextColor.fromLegacyFormat(ChatFormatting.AQUA);

    /** Name, or ID if unnamed, in its colour */
    public static MutableComponent displayName(ResourceLocation cosmeticId) {
        CosmeticDefinition def = CosmeticDataLoader.getDefinition(cosmeticId);
        String name = def != null ? def.name().orElse(cosmeticId.toString()) : cosmeticId.toString();
        TextColor color = def != null ? def.color().orElse(DEFAULT_NAME_COLOR) : DEFAULT_NAME_COLOR;
        return Component.literal(name).withStyle(style -> style.withColor(color));
    }
}
