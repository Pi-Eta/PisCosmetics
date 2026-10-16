package com.pieta.piscosmetics.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.pieta.piscosmetics.api.CosmeticSlotNames;
import com.pieta.piscosmetics.client.renderer.CosmeticRenderer;
import com.pieta.piscosmetics.item.CosmeticArmorItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Draws cosmetics worn in the vanilla armor slots. Uses no Accessories types, so it works without it. */
@Mixin(HumanoidArmorLayer.class)
public class CosmeticArmorRenderMixin {

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
            at = @At("HEAD"))
    private void piscosmetics$renderCosmeticArmor(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                                                  LivingEntity livingEntity, float limbSwing, float limbSwingAmount,
                                                  float partialTick, float ageInTicks, float netHeadYaw, float headPitch,
                                                  CallbackInfo ci) {
        HumanoidArmorLayer layer = (HumanoidArmorLayer)(Object)this;
        HumanoidModel<?> parentModel = (HumanoidModel<?>) layer.getParentModel();

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) continue;

            ItemStack stack = livingEntity.getItemBySlot(slot);
            // The four armor-slot items, or the universal item when its JSON names an armor slot
            if (!(stack.getItem() instanceof CosmeticArmorItem) && !CosmeticSlotNames.isUniversal(stack)) continue;

            CosmeticRenderer.render(stack, livingEntity, poseStack, parentModel, bufferSource, packedLight);
        }
    }
}
