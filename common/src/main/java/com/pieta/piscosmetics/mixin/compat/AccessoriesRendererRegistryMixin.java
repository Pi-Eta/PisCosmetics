package com.pieta.piscosmetics.mixin.compat;

import com.pieta.piscosmetics.compat.accessories.StandInRenderer;
import com.pieta.piscosmetics.item.CosmeticHelper;
import io.wispforest.accessories.api.client.AccessoryRenderer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Desc;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Other mods' worn items that resolve to a cosmetic get StandInRenderer.
 * Pseudo: skipped without Accessories. Desc: remapped on Fabric. require = 0: no crash if Accessories changes
 */
@Pseudo
@Mixin(targets = "io.wispforest.accessories.api.client.AccessoriesRendererRegistry", remap = false)
public abstract class AccessoriesRendererRegistryMixin {

    @Inject(target = @Desc(value = "getRenderer", args = ItemStack.class, ret = AccessoryRenderer.class),
            at = @At("HEAD"), cancellable = true, require = 0)
    private static void piscosmetics$standIn(ItemStack stack, CallbackInfoReturnable<AccessoryRenderer> cir) {
        if (CosmeticHelper.getStandInId(stack) != null) {
            cir.setReturnValue(StandInRenderer.INSTANCE);
        }
    }
}
