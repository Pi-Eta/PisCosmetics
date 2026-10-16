package com.pieta.piscosmetics.api;

import net.minecraft.world.entity.player.Player;

import java.util.HashSet;
import java.util.Set;

public class CosmeticArmorHelper {

    /**
     * Searches for cosmetics wherever they're worn (see CosmeticSlots)
     */
    public static Set<String> getHiddenArmorSlots(Player player) {
        Set<String> hidden = new HashSet<>();
        for (CosmeticDefinition def : CosmeticSlots.getWorn(player)) {
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
