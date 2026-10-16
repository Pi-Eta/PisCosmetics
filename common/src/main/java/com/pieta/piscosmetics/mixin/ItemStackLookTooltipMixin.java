package com.pieta.piscosmetics.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.pieta.piscosmetics.item.CosmeticHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Adds "Look: <name>" to items with an applied look
 */
@Mixin(ItemStack.class)
public abstract class ItemStackLookTooltipMixin {

    @Inject(method = "getTooltipLines(Lnet/minecraft/world/item/Item$TooltipContext;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/TooltipFlag;)Ljava/util/List;",
            at = @At("RETURN"))
    private void piscosmetics$lookLine(Item.TooltipContext context, Player player, TooltipFlag flag,
                                       CallbackInfoReturnable<List<Component>> cir) {
        // Polymer builds tooltips on the server too, which shares this game in singleplayer
        if (!RenderSystem.isOnRenderThread()) return;
        ResourceLocation look = CosmeticHelper.getAppliedLook((ItemStack) (Object) this);
        if (look == null) return;
        List<Component> lines = cir.getReturnValue();
        if (lines.isEmpty()) return; // hidden tooltip, immutable list
        lines.add(Math.min(1, lines.size()),
                Component.translatable("tooltip.piscosmetics.look", CosmeticHelper.displayName(look)).withStyle(ChatFormatting.GRAY));
    }
}
