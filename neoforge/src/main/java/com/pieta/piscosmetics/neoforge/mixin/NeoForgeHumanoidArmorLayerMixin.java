package com.pieta.piscosmetics.neoforge.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.pieta.piscosmetics.api.CosmeticArmorHelper;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hides vanilla armor pieces that a worn cosmetic asks to hide (hide_armor)
 */
@Mixin(HumanoidArmorLayer.class)
public class NeoForgeHumanoidArmorLayerMixin {

    @Inject(method = "renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/model/HumanoidModel;FFFFFF)V",
            at = @At("HEAD"), cancellable = true)
    private void piscosmetics$cancelArmorPiece(PoseStack poseStack, MultiBufferSource bufferSource, LivingEntity livingEntity,
                                               EquipmentSlot slot, int packedLight, HumanoidModel<?> model,
                                               float limbSwing, float limbSwingAmount, float partialTick,
                                               float ageInTicks, float netHeadYaw, float headPitch,
                                               CallbackInfo ci) {
        if (!(livingEntity instanceof Player player)) return;

        String slotName = switch (slot) {
            case HEAD -> "head";
            case CHEST -> "chest";
            case LEGS -> "legs";
            case FEET -> "feet";
            default -> null;
        };
        if (slotName != null && CosmeticArmorHelper.shouldHideSlot(player, slotName)) {
            ci.cancel();
        }
    }
}
