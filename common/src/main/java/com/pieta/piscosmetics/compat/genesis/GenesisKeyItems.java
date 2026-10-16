package com.pieta.piscosmetics.compat.genesis;

import com.pieta.piscosmetics.api.CosmeticSlotNames;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

/**
 * Which Accessories slot each Genesis Forms key item goes in. Plain data, no Genesis Forms code.
 * <p>
 * Keep this in sync with the "genesisforms:" entries in data/accessories/tags/item (wrist, ring, necklace, belt).
 * The server uses those tags; players' clients use this table, because on a client Genesis Forms items are
 * Polymer-disguised vanilla items that no tag lists.
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

    private GenesisKeyItems() {}

    /** True if this Genesis Forms item may go in this Accessories slot. */
    public static boolean fits(ResourceLocation itemId, String slotName) {
        if (itemId == null || !itemId.getNamespace().equals("genesisforms")) return false;
        String slot = SLOT_OF_ITEM.get(itemId.getPath());
        return slot != null && slot.equals(CosmeticSlotNames.normalize(slotName));
    }
}
