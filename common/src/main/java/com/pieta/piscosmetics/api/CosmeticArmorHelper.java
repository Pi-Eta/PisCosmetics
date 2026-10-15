package com.pieta.piscosmetics.api;

import com.pieta.piscosmetics.data.CosmeticDataLoader;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.slot.SlotEntryReference;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.HashSet;
import java.util.Set;

public class CosmeticArmorHelper {

    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    /**
     * Searches for cosmetics in accessory slots and in vanilla armour slots
     */
    public static Set<String> getHiddenArmorSlots(Player player) {
        Set<String> hidden = new HashSet<>();

        AccessoriesCapability capability = AccessoriesCapability.get(player);
        if (capability != null) {
            for (SlotEntryReference reference : capability.getAllEquipped()) {
                addHiddenSlots(reference.stack(), hidden);
            }
        }

        for (EquipmentSlot slot : ARMOR_SLOTS) {
            addHiddenSlots(player.getItemBySlot(slot), hidden);
        }
        return hidden;
    }

    public static boolean shouldHideSlot(Player player, String slot) {
        return getHiddenArmorSlots(player).contains(slot);
    }

    private static void addHiddenSlots(ItemStack stack, Set<String> hidden) {
        if (stack.isEmpty()) return;

        String cosmeticId = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                .copyTag().getString("cosmetic");
        if (cosmeticId.isEmpty()) return;

        ResourceLocation id = ResourceLocation.tryParse(cosmeticId);
        if (id == null) return;

        CosmeticDefinition def = CosmeticDataLoader.getDefinition(id);
        if (def == null) return;

        for (String slot : def.hideArmor()) {
            if (slot.equals("all")) {
                hidden.add("head");
                hidden.add("chest");
                hidden.add("legs");
                hidden.add("feet");
                hidden.add("elytra");
            } else {
                hidden.add(slot);
            }
        }
    }
}