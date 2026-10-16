package com.pieta.piscosmetics.compat.accessories;

import com.mojang.blaze3d.vertex.PoseStack;
import com.pieta.piscosmetics.client.renderer.CosmeticRenderer;
import com.pieta.piscosmetics.item.CosmeticHelper;
import io.wispforest.accessories.api.client.AccessoryRenderer;
import io.wispforest.accessories.api.slot.SlotReference;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Draws a cosmetic in place of another mod's worn item (a Genesis Forms Key Stone, say).
 * AccessoriesRendererRegistryMixin hands out this renderer only for stacks that resolve to a cosmetic.
 */
public class StandInRenderer implements AccessoryRenderer {

    public static final StandInRenderer INSTANCE = new StandInRenderer();

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
        ResourceLocation cosmeticId = CosmeticHelper.getStandInId(stack);
        if (cosmeticId != null && entityModel instanceof HumanoidModel<?> humanoidModel) {
            CosmeticRenderer.render(CosmeticHelper.displayStack(cosmeticId), reference.entity(), poseStack,
                    humanoidModel, bufferSource, packedLight);
        }
    }
}
