package com.pieta.piscosmetics.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.pieta.piscosmetics.client.renderer.CosmeticAccessoryRenderer;
import com.pieta.piscosmetics.item.CosmeticArmorItem;
import io.wispforest.accessories.api.slot.SlotReference;
import net.minecraft.client.model.EntityModel;
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

@Mixin(HumanoidArmorLayer.class)
public class CosmeticArmorRenderMixin {

    private final CosmeticAccessoryRenderer RENDERER = new CosmeticAccessoryRenderer();

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
            if (!(stack.getItem() instanceof CosmeticArmorItem)) continue;

            String slotName = switch (slot) {
                case HEAD -> "head";
                case CHEST -> "chest";
                case LEGS -> "legs";
                case FEET -> "feet";
                default -> slot.getName();
            };

            SlotReference ref = SlotReference.of(livingEntity, slotName, 0);
            RENDERER.render(stack, ref, poseStack, parentModel, bufferSource,
                    packedLight, limbSwing, limbSwingAmount, partialTick,
                    ageInTicks, netHeadYaw, headPitch);
        }
    }
}