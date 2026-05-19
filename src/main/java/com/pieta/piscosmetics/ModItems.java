package com.pieta.piscosmetics;

import com.pieta.piscosmetics.item.CosmeticItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems("piscosmetics");

    // Generic cosmetic items
    public static final DeferredItem<CosmeticItem> COSMETIC_ANKLET =
            ITEMS.register("cosmetic_anklet",
                    () -> new CosmeticItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<CosmeticItem> COSMETIC_BACK =
            ITEMS.register("cosmetic_back",
                    () -> new CosmeticItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<CosmeticItem> COSMETIC_BELT =
            ITEMS.register("cosmetic_belt",
                    () -> new CosmeticItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<CosmeticItem> COSMETIC_CAPE =
            ITEMS.register("cosmetic_cape",
                    () -> new CosmeticItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<CosmeticItem> COSMETIC_CHARM =
            ITEMS.register("cosmetic_charm",
                    () -> new CosmeticItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<CosmeticItem> COSMETIC_FACE =
            ITEMS.register("cosmetic_face",
                    () -> new CosmeticItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<CosmeticItem> COSMETIC_HAND =
            ITEMS.register("cosmetic_hand",
                    () -> new CosmeticItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<CosmeticItem> COSMETIC_HAT =
            ITEMS.register("cosmetic_hat",
                    () -> new CosmeticItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<CosmeticItem> COSMETIC_NECKLACE =
            ITEMS.register("cosmetic_necklace",
                    () -> new CosmeticItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<CosmeticItem> COSMETIC_RING =
            ITEMS.register("cosmetic_ring",
                    () -> new CosmeticItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<CosmeticItem> COSMETIC_SHOES =
            ITEMS.register("cosmetic_shoes",
                    () -> new CosmeticItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<CosmeticItem> COSMETIC_WRIST =
            ITEMS.register("cosmetic_wrist",
                    () -> new CosmeticItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<CosmeticItem> COSMETIC_DYNAMAX_BAND =
            ITEMS.register("cosmetic_dynamax_band",
                    () -> new CosmeticItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<CosmeticItem> COSMETIC_MEGA_BRACELET =
            ITEMS.register("cosmetic_mega_bracelet",
                    () -> new CosmeticItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<CosmeticItem> COSMETIC_TERA_ORB =
            ITEMS.register("cosmetic_tera_orb",
                    () -> new CosmeticItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<CosmeticItem> COSMETIC_Z_RING =
            ITEMS.register("cosmetic_z_ring",
                    () -> new CosmeticItem(new Item.Properties().stacksTo(1)));


}