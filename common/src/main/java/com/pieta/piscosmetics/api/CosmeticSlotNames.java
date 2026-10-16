package com.pieta.piscosmetics.api;

import com.pieta.piscosmetics.ModItems;
import com.pieta.piscosmetics.item.CosmeticHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Slot names, and the defaults that come with each slot.
 * <p>
 * Each of the 20 original items is named after the slot it fits, and its model, offsets and attach point
 * are that slot's defaults. The universal item ({@code piscosmetics:cosmetic}) takes its slot from the
 * cosmetic's JSON instead, and borrows the defaults of the original item for that slot.
 */
public final class CosmeticSlotNames {

    /** Slot key -> the original item whose model, offsets and attach point that slot uses. */
    private static final Map<String, String> LEGACY_ITEM = Map.ofEntries(
            Map.entry("anklet", "cosmetic_anklet"),
            Map.entry("back", "cosmetic_back"),
            Map.entry("belt", "cosmetic_belt"),
            Map.entry("cape", "cosmetic_cape"),
            Map.entry("charm", "cosmetic_charm"),
            Map.entry("face", "cosmetic_face"),
            Map.entry("hand", "cosmetic_hand"),
            Map.entry("hat", "cosmetic_hat"),
            Map.entry("necklace", "cosmetic_necklace"),
            Map.entry("ring", "cosmetic_ring"),
            Map.entry("shoes", "cosmetic_shoes"),
            Map.entry("wrist", "cosmetic_wrist"),
            Map.entry("dynamax_slot", "cosmetic_dynamax_band"),
            Map.entry("mega_slot", "cosmetic_mega_bracelet"),
            Map.entry("tera_slot", "cosmetic_tera_orb"),
            Map.entry("z_slot", "cosmetic_z_ring"),
            Map.entry("head", "cosmetic_head"),
            Map.entry("chest", "cosmetic_chest"),
            Map.entry("legs", "cosmetic_legs"),
            Map.entry("feet", "cosmetic_feet"));

    /** The reverse: original item name -> its slot key. */
    private static final Map<String, String> SLOT_OF_ITEM = new HashMap<>();
    static {
        LEGACY_ITEM.forEach((slot, item) -> SLOT_OF_ITEM.put(item, slot));
    }

    private static final String DEFAULT_ITEM = "cosmetic_charm";

    private CosmeticSlotNames() {}

    /** "mega_showdown:z-slot" and "Z_SLOT" both become "z_slot". */
    public static String normalize(String slot) {
        int colon = slot.indexOf(':');
        return (colon >= 0 ? slot.substring(colon + 1) : slot).replace('-', '_').toLowerCase(Locale.ROOT);
    }

    /** True for the universal item, whose slot comes from its JSON. */
    public static boolean isUniversal(ItemStack stack) {
        return ModItems.COSMETIC != null && stack.is(ModItems.COSMETIC);
    }

    /**
     * The slot this stack belongs to: from its JSON for the universal item, from its ID for the 20 originals.
     * Empty when the universal item has no (known) definition yet.
     */
    public static String slotKey(ItemStack stack) {
        if (isUniversal(stack)) {
            CosmeticDefinition def = CosmeticHelper.getDefinition(stack);
            return def != null ? normalize(def.slot()) : "";
        }
        String itemName = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        return SLOT_OF_ITEM.getOrDefault(itemName, itemName);
    }

    /** The original item to borrow defaults from, e.g. "cosmetic_hat" for "hat". */
    public static String legacyItemFor(String slotKey) {
        return LEGACY_ITEM.getOrDefault(slotKey, DEFAULT_ITEM);
    }

    /** HEAD, CHEST, LEGS or FEET for the vanilla armor slots, null for every other slot. */
    public static EquipmentSlot armorSlot(String slotKey) {
        return switch (slotKey) {
            case "head" -> EquipmentSlot.HEAD;
            case "chest" -> EquipmentSlot.CHEST;
            case "legs" -> EquipmentSlot.LEGS;
            case "feet" -> EquipmentSlot.FEET;
            default -> null;
        };
    }
}
