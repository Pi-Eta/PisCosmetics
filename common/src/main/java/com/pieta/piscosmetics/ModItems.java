package com.pieta.piscosmetics;

import com.pieta.piscosmetics.item.CosmeticArmorItem;
import com.pieta.piscosmetics.item.CosmeticItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * All of the mod's items. Neoforge only allows registration during RegisterEvent so that's the method.
 */
public final class ModItems {

    // Accessories-slot items
    public static CosmeticItem COSMETIC_ANKLET;
    public static CosmeticItem COSMETIC_BACK;
    public static CosmeticItem COSMETIC_BELT;
    public static CosmeticItem COSMETIC_CAPE;
    public static CosmeticItem COSMETIC_CHARM;
    public static CosmeticItem COSMETIC_FACE;
    public static CosmeticItem COSMETIC_HAND;
    public static CosmeticItem COSMETIC_HAT;
    public static CosmeticItem COSMETIC_NECKLACE;
    public static CosmeticItem COSMETIC_RING;
    public static CosmeticItem COSMETIC_SHOES;
    public static CosmeticItem COSMETIC_WRIST;

    // Mega Showdown slot items
    public static CosmeticItem COSMETIC_DYNAMAX_BAND;
    public static CosmeticItem COSMETIC_MEGA_BRACELET;
    public static CosmeticItem COSMETIC_TERA_ORB;
    public static CosmeticItem COSMETIC_Z_RING;

    // Vanilla armor slot items
    public static CosmeticArmorItem COSMETIC_HEAD;
    public static CosmeticArmorItem COSMETIC_CHEST;
    public static CosmeticArmorItem COSMETIC_LEGS;
    public static CosmeticArmorItem COSMETIC_FEET;

    // The universal item: its slot comes from the cosmetic's JSON "slot" field (CosmeticSlotNames)
    public static CosmeticItem COSMETIC;

    private static final List<Item> ALL_ITEMS = new ArrayList<>();

    /** Every item in order. */
    public static final List<Item> ALL = Collections.unmodifiableList(ALL_ITEMS);

    private ModItems() {}

    public static void register(BiConsumer<ResourceLocation, Item> registrar) {
        COSMETIC_ANKLET = add(registrar, "cosmetic_anklet", new CosmeticItem(props()));
        COSMETIC_BACK = add(registrar, "cosmetic_back", new CosmeticItem(props()));
        COSMETIC_BELT = add(registrar, "cosmetic_belt", new CosmeticItem(props()));
        COSMETIC_CAPE = add(registrar, "cosmetic_cape", new CosmeticItem(props()));
        COSMETIC_CHARM = add(registrar, "cosmetic_charm", new CosmeticItem(props()));
        COSMETIC_FACE = add(registrar, "cosmetic_face", new CosmeticItem(props()));
        COSMETIC_HAND = add(registrar, "cosmetic_hand", new CosmeticItem(props()));
        COSMETIC_HAT = add(registrar, "cosmetic_hat", new CosmeticItem(props()));
        COSMETIC_NECKLACE = add(registrar, "cosmetic_necklace", new CosmeticItem(props()));
        COSMETIC_RING = add(registrar, "cosmetic_ring", new CosmeticItem(props()));
        COSMETIC_SHOES = add(registrar, "cosmetic_shoes", new CosmeticItem(props()));
        COSMETIC_WRIST = add(registrar, "cosmetic_wrist", new CosmeticItem(props()));

        COSMETIC_DYNAMAX_BAND = add(registrar, "cosmetic_dynamax_band", new CosmeticItem(props()));
        COSMETIC_MEGA_BRACELET = add(registrar, "cosmetic_mega_bracelet", new CosmeticItem(props()));
        // Mega Showdown stores a Tera Orb's charge as damage (+10 per use, usable below 100), like its own orb
        COSMETIC_TERA_ORB = add(registrar, "cosmetic_tera_orb", new CosmeticItem(new Item.Properties().durability(100)));
        COSMETIC_Z_RING = add(registrar, "cosmetic_z_ring", new CosmeticItem(props()));

        COSMETIC_HEAD = add(registrar, "cosmetic_head", new CosmeticArmorItem(props(), EquipmentSlot.HEAD));
        COSMETIC_CHEST = add(registrar, "cosmetic_chest", new CosmeticArmorItem(props(), EquipmentSlot.CHEST));
        COSMETIC_LEGS = add(registrar, "cosmetic_legs", new CosmeticArmorItem(props(), EquipmentSlot.LEGS));
        COSMETIC_FEET = add(registrar, "cosmetic_feet", new CosmeticArmorItem(props(), EquipmentSlot.FEET));

        COSMETIC = add(registrar, "cosmetic", new CosmeticItem(props()));
    }

    private static Item.Properties props() {
        return new Item.Properties().stacksTo(1);
    }

    private static <T extends Item> T add(BiConsumer<ResourceLocation, Item> registrar, String name, T item) {
        registrar.accept(ResourceLocation.fromNamespaceAndPath(PisCosmetics.MODID, name), item);
        ALL_ITEMS.add(item);
        return item;
    }
}
