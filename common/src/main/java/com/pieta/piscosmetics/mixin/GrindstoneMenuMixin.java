package com.pieta.piscosmetics.mixin;

import com.pieta.piscosmetics.item.CosmeticHelper;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Grindstone removes an applied look from a single item. Nothing is given back
 */
@Mixin(GrindstoneMenu.class)
public abstract class GrindstoneMenuMixin {

    @Inject(method = "computeResult(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;",
            at = @At("HEAD"), cancellable = true)
    private void piscosmetics$removeLook(ItemStack top, ItemStack bottom, CallbackInfoReturnable<ItemStack> cir) {
        if (!top.isEmpty() && !bottom.isEmpty()) return; // vanilla merge
        ItemStack input = top.isEmpty() ? bottom : top;
        if (input.getCount() == 1 && CosmeticHelper.getAppliedLook(input) != null) {
            cir.setReturnValue(CosmeticHelper.withoutLook(input));
        }
    }
}
