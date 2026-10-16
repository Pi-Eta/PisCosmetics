package com.pieta.piscosmetics.mixin;

import com.pieta.piscosmetics.api.CosmeticSlotNames;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Lets the universal item go in a vanilla armor slot when its JSON says "slot": "head" (or chest, legs, feet).
 * <p>
 * The inventory's armor slots, shift-clicking and dispensers all ask getEquipmentSlotForItem where an item
 * belongs. NeoForge adds its own check at the start of this method, but HEAD runs before it, so this one
 * mixin works on both loaders. It runs on both sides, because the server decides what fits too.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityCosmeticSlotMixin {

    @Inject(method = "getEquipmentSlotForItem(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/entity/EquipmentSlot;",
            at = @At("HEAD"), cancellable = true)
    private void piscosmetics$cosmeticSlot(ItemStack stack, CallbackInfoReturnable<EquipmentSlot> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        // Players and armor stands only, so mobs don't pick up and wear dropped cosmetics
        if (!(self instanceof Player) && !(self instanceof ArmorStand)) return;
        if (CosmeticSlotNames.isUniversal(stack)) {
            EquipmentSlot slot = CosmeticSlotNames.armorSlot(CosmeticSlotNames.slotKey(stack));
            if (slot != null && self.canUseSlot(slot)) cir.setReturnValue(slot);
        }
    }
}
