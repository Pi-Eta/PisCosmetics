package com.pieta.piscosmetics.item;

import com.pieta.piscosmetics.ModItems;
import com.pieta.piscosmetics.api.CosmeticDefinition;
import com.pieta.piscosmetics.api.CosmeticSlotNames;
import com.pieta.piscosmetics.data.CosmeticDataLoader;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.HashMap;
import java.util.Map;

public class CosmeticHelper {

    /** Where Polymer keeps the real item inside a disguised one (server-only mods like Genesis Forms). */
    private static final String POLYMER_STACK = "$polymer:stack";

    /** Cached universal items used to draw a cosmetic in place of another mod's item. */
    private static final Map<ResourceLocation, ItemStack> DISPLAY_STACKS = new HashMap<>();

    public static CosmeticDefinition getDefinition(ItemStack stack) {
        ResourceLocation id = getCosmeticId(stack);
        return id == null ? null : CosmeticDataLoader.getDefinition(id);
    }

    /**
     * Which cosmetic this stack shows, or null. In order:
     * 1. the stack's own "cosmetic" custom data (your items, or another mod's item given a look),
     * 2. the same, inside a Polymer disguise (what players' clients receive for Genesis Forms items),
     * 3. the default look for the item type, from a cosmetic's render_for.
     * The same answer comes out on the server (real item) and on a player's client (disguised item).
     */
    public static ResourceLocation getCosmeticId(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        // Read without copying (this runs every frame for every worn item); nothing here changes the tag
        CompoundTag data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).getUnsafe();
        CompoundTag hidden = data.getCompound(POLYMER_STACK);

        String cosmeticId = data.getString("cosmetic");
        if (cosmeticId.isEmpty()) {
            cosmeticId = hidden.getCompound("components").getCompound("minecraft:custom_data").getString("cosmetic");
        }
        if (!cosmeticId.isEmpty()) return ResourceLocation.tryParse(cosmeticId);

        String hiddenItemId = hidden.getString("id");
        ResourceLocation itemId = !hiddenItemId.isEmpty()
                ? ResourceLocation.tryParse(hiddenItemId)               // client: the real ID inside the disguise
                : BuiltInRegistries.ITEM.getKey(stack.getItem());       // server, or a normal item
        return CosmeticDataLoader.getStandIn(itemId);
    }

    /** The cosmetic to draw in place of another mod's item (never one of ours), or null. */
    public static ResourceLocation getStandInId(ItemStack stack) {
        if (stack.getItem() instanceof CosmeticItem) return null;
        ResourceLocation id = getCosmeticId(stack);
        // A look whose cosmetic no longer exists (removed datapack, typo) isn't drawn; the item renders as normal
        return id != null && CosmeticDataLoader.getDefinition(id) != null ? id : null;
    }

    /** True for an item Polymer has disguised as a vanilla one. */
    public static boolean isDisguised(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).contains(POLYMER_STACK);
    }

    /** The real item ID inside a Polymer disguise, or null if the stack isn't disguised. */
    public static ResourceLocation getDisguisedItemId(ItemStack stack) {
        if (!isDisguised(stack)) return null;
        String id = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).getUnsafe()
                .getCompound(POLYMER_STACK).getString("id");
        return id.isEmpty() ? null : ResourceLocation.tryParse(id);
    }

    /** A universal item showing this cosmetic, so stand-ins are drawn exactly like any other cosmetic. */
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
                System.out.println("ANIM PATH = " + "animations/" + animId.getPath() + ".animation.json");
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

    public static net.minecraft.network.chat.Component getName(ItemStack stack, net.minecraft.network.chat.Component fallback) {
        CosmeticDefinition def = getDefinition(stack);
        if (def != null && def.name().isPresent()) {
            return net.minecraft.network.chat.Component.literal(def.name().get());
        }
        return fallback;
    }
}