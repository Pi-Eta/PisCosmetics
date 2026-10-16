package com.pieta.piscosmetics.mixin.compat;

import com.pieta.piscosmetics.compat.genesis.GenesisKeyItems;
import com.pieta.piscosmetics.item.CosmeticHelper;
import io.wispforest.accessories.api.slot.SlotType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Desc;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

/**
 * Client only. Disguised Genesis Forms key items aren't in any slot tag, so slots come from GenesisKeyItems.
 * require = 0 so an Accessories change doesn't crash the game
 */
@Pseudo
@Mixin(targets = "io.wispforest.accessories.api.AccessoriesAPI", remap = false)
public abstract class AccessoriesAPIMixin {

    @Inject(target = @Desc(value = "getPredicateResults",
                    args = {Set.class, Level.class, LivingEntity.class, SlotType.class, int.class, ItemStack.class},
                    ret = boolean.class),
            at = @At("HEAD"), cancellable = true, require = 0)
    private static void piscosmetics$letServerDecide(Set<ResourceLocation> predicateIds, Level level, LivingEntity entity,
                                                     SlotType slotType, int index, ItemStack stack,
                                                     CallbackInfoReturnable<Boolean> cir) {
        if (level != null && level.isClientSide()
                && GenesisKeyItems.fits(CosmeticHelper.getDisguisedItemId(stack), slotType.name())) {
            cir.setReturnValue(true);
        }
    }
}
