package com.pieta.piscosmetics.compat.accessories;

import com.mojang.blaze3d.vertex.PoseStack;
import com.pieta.piscosmetics.client.renderer.CosmeticRenderer;
import io.wispforest.accessories.api.client.AccessoryRenderer;
import io.wispforest.accessories.api.slot.SlotReference;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/** Lets Accessories draw a cosmetic worn in one of its slots, using the shared {@link CosmeticRenderer}. */
public class CosmeticAccessoryRenderer implements AccessoryRenderer {

    @Override
    public <M extends LivingEntity> void render(
            ItemStack stack,
            SlotReference reference,
            PoseStack poseStack,
            EntityModel<M> entityModel,
            MultiBufferSource bufferSource,
            int packedLight,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        if (entityModel instanceof HumanoidModel<?> humanoidModel) {
            CosmeticRenderer.render(stack, reference.entity(), poseStack, humanoidModel, bufferSource, packedLight);
        }
    }
}
