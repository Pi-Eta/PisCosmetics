package com.pieta.piscosmetics.api;

import com.pieta.piscosmetics.data.CosmeticDataLoader;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.slot.SlotEntryReference;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.HashSet;
import java.util.Set;

public class CosmeticArmorHelper {

    public static Set<String> getHiddenArmorSlots(Player player) {
        Set<String> hidden = new HashSet<>();
        AccessoriesCapability capability = AccessoriesCapability.get(player);
        if (capability == null) return hidden;

        for (SlotEntryReference reference : capability.getAllEquipped()) {
            ItemStack stack = reference.stack();
            String cosmeticId = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                    .copyTag().getString("cosmetic");
            if (cosmeticId.isEmpty()) continue;

            ResourceLocation id = ResourceLocation.tryParse(cosmeticId);
            if (id == null) continue;

            CosmeticDefinition def = CosmeticDataLoader.getDefinition(id);
            if (def == null) continue;

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
        return hidden;
    }

    public static boolean shouldHideSlot(Player player, String slot) {
        return getHiddenArmorSlots(player).contains(slot);
    }
}