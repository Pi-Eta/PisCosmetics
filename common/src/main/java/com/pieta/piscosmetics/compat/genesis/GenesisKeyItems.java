package com.pieta.piscosmetics.compat.genesis;

import com.pieta.piscosmetics.api.CosmeticSlotNames;
import com.pieta.piscosmetics.api.KeyItemTypes;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

/**
 * Slot and key item type of each Genesis Forms key item, for clients, where they're disguised and in no tag.
 * Keep in sync with the genesisforms entries in data/accessories/tags/item and data/piscosmetics/tags/item/key_items
 */
public final class GenesisKeyItems {

    private static final Map<String, String> SLOT_OF_ITEM = Map.of(
            "mega_bracelet", "wrist",
            "mega_cuff", "wrist",
            "dynamax_band", "wrist",
            "mega_ring", "ring",
            "z_ring", "ring",
            "z_power_ring", "ring",
            "key_stone", "ring",
            "mega_charm", "necklace",
            "tera_orb", "belt");

    private static final Map<String, String> TYPE_OF_ITEM = Map.of(
            "key_stone", KeyItemTypes.MEGA,
            "mega_bracelet", KeyItemTypes.MEGA,
            "mega_ring", KeyItemTypes.MEGA,
            "mega_cuff", KeyItemTypes.MEGA,
            "mega_charm", KeyItemTypes.MEGA,
            "z_ring", KeyItemTypes.Z_MOVE,
            "z_power_ring", KeyItemTypes.Z_MOVE,
            "dynamax_band", KeyItemTypes.DYNAMAX,
            "tera_orb", KeyItemTypes.TERA);

    private GenesisKeyItems() {}

    /** KeyItemTypes type of a Genesis Forms item, or null */
    public static String typeOf(ResourceLocation itemId) {
        if (itemId == null || !itemId.getNamespace().equals("genesisforms")) return null;
        return TYPE_OF_ITEM.get(itemId.getPath());
    }

    /** True if this Genesis Forms item fits this Accessories slot */
    public static boolean fits(ResourceLocation itemId, String slotName) {
        if (itemId == null || !itemId.getNamespace().equals("genesisforms")) return false;
        String slot = SLOT_OF_ITEM.get(itemId.getPath());
        return slot != null && slot.equals(CosmeticSlotNames.normalize(slotName));
    }
}
