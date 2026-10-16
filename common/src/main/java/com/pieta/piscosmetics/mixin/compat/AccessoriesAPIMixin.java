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
 * Client only. A player's client sees a Polymer-disguised item (a Genesis Forms Key Stone) as the vanilla item
 * it's disguised as, which no slot tag lists, so the Accessories screen would refuse it. For disguised Genesis
 * Forms key items, the client answers from GenesisKeyItems instead, the same slots the server's tags allow.
 * Every other stack is left alone. require = 0: if Accessories changes this method, the game still starts.
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
