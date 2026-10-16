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
 * Accessories picks the renderer for every worn stack in getRenderer(ItemStack), with no event to change it.
 * If a worn stack from another mod resolves to a cosmetic (render_for, or its own "cosmetic" data), draw that
 * cosmetic instead. Every other stack keeps the renderer it had.
 * <p>
 * Pseudo: skipped when Accessories isn't installed. Desc instead of a descriptor string: its class references
 * get remapped on Fabric like normal code, which a string pointing into another mod might not.
 * require = 0: if Accessories changes this method, the game still starts (stand-ins just aren't drawn).
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
