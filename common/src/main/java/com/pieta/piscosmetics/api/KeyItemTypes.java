package com.pieta.piscosmetics.api;

import com.pieta.piscosmetics.PisCosmetics;
import com.pieta.piscosmetics.compat.genesis.GenesisKeyItems;
import com.pieta.piscosmetics.item.CosmeticHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Key item types for "key_items", each an item tag in data/piscosmetics/tags/item/key_items
 */
public final class KeyItemTypes {

    public static final String MEGA = "mega";
    public static final String Z_MOVE = "z_move";
    public static final String DYNAMAX = "dynamax";
    public static final String TERA = "tera";

    public static final List<String> ALL = List.of(MEGA, Z_MOVE, DYNAMAX, TERA);

    private KeyItemTypes() {}

    public static TagKey<Item> tag(String type) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(PisCosmetics.MODID, "key_items/" + type));
    }

    /** Entries that aren't a known type */
    public static List<String> unknown(List<String> types) {
        return types.stream().filter(type -> !ALL.contains(type)).toList();
    }

    /** True if the item is a key item of one of these types */
    public static boolean isOneOf(ItemStack stack, List<String> types) {
        // Disguised Genesis Forms items aren't in any tag on the client
        String genesisType = GenesisKeyItems.typeOf(CosmeticHelper.getDisguisedItemId(stack));
        if (genesisType != null) return types.contains(genesisType);
        for (String type : types) {
            if (stack.is(tag(type))) return true;
        }
        return false;
    }
}
