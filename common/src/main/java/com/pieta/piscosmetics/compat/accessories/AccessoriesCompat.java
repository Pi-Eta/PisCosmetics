package com.pieta.piscosmetics.compat.accessories;

import com.pieta.piscosmetics.ModItems;
import com.pieta.piscosmetics.api.CosmeticDefinition;
import com.pieta.piscosmetics.api.CosmeticSlotNames;
import com.pieta.piscosmetics.api.CosmeticSlots;
import com.pieta.piscosmetics.item.CosmeticHelper;
import io.wispforest.accessories.api.AccessoriesAPI;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.Accessory;
import io.wispforest.accessories.api.client.AccessoriesRendererRegistry;
import io.wispforest.accessories.api.slot.SlotReference;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Everything that needs the Accessories mod.
 * <p>
 * Only ever called behind {@code Services.PLATFORM.isModLoaded("accessories")}. Java loads a class the
 * first time code needs it, so as long as nothing else mentions an Accessories type, the mod runs fine
 * without Accessories installed. Keep every {@code io.wispforest} usage inside this package.
 */
public final class AccessoriesCompat {

    private AccessoriesCompat() {}

    /** Both sides, after items are registered. */
    public static void init() {
        // Cosmetics in Accessories slots count as worn (hide_armor, particles)
        CosmeticSlots.addSource((entity, out) -> {
            AccessoriesCapability capability = AccessoriesCapability.get(entity);
            if (capability != null) capability.getAllEquipped().forEach(reference -> out.accept(reference.stack()));
        });

        // The universal item is in every slot tag (data/accessories/tags/item), so every slot would accept it.
        // canEquip narrows that to the one slot its JSON names. Runs on both sides; the client knows
        // each cosmetic's slot because CosmeticSyncPacket sends the definitions.
        AccessoriesAPI.registerAccessory(ModItems.COSMETIC, new Accessory() {
            @Override
            public boolean canEquip(ItemStack stack, SlotReference reference) {
                CosmeticDefinition def = CosmeticHelper.getDefinition(stack);
                return def != null && CosmeticSlotNames.normalize(def.slot())
                        .equals(CosmeticSlotNames.normalize(reference.slotName()));
            }
        });
    }

    /** Client only, after items are registered: Accessories draws every cosmetic with our renderer. */
    public static void initClient() {
        for (Item item : ModItems.ALL) {
            AccessoriesRendererRegistry.registerRenderer(item, CosmeticAccessoryRenderer::new);
        }
    }
}
