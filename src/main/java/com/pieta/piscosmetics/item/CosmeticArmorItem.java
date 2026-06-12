package com.pieta.piscosmetics.item;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;

public class CosmeticArmorItem extends CosmeticItem implements Equipable {
    private final EquipmentSlot slot;

    public CosmeticArmorItem(Properties properties, EquipmentSlot slot) {
        super(properties);
        this.slot = slot;
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return this.slot;
    }
}