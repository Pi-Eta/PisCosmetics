package com.pieta.piscosmetics.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.pieta.piscosmetics.api.CosmeticDefinition;
import com.pieta.piscosmetics.data.CosmeticDataLoader;
import io.wispforest.accessories.api.client.AccessoryRenderer;
import io.wispforest.accessories.api.slot.SlotReference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.particles.ParticleOptions;

public class CosmeticAccessoryRenderer implements AccessoryRenderer {

    private record SlotDefaults(
            float transX, float transY, float transZ,
            float rotX, float rotY, float rotZ,
            float scale
    ) {}

    private static SlotDefaults getDefaults(String itemId) {
        return switch (itemId) {
            case "cosmetic_hat" ->
                    new SlotDefaults(0.0F, -7.01F/16F, 0.0F, 180.0F, 180.0F, 0F, 1.0F);
            case "cosmetic_anklet" ->
                    new SlotDefaults(0.0F, 9.0F/16F, 0.0F, 180.0F, 180.0F, 0F, 1.0F);
            case "cosmetic_back" ->
                    new SlotDefaults(0.0F, 9.0F/16F, 2.0F/16F, 180.0F, 180.0F, 0F, 1.0F);
            case "cosmetic_belt" ->
                    new SlotDefaults(-1.0F/16F, 0.5F, 0.0F, 180.0F, 180.0F, 0F, 1.0F);
            case "cosmetic_cape" ->
                    new SlotDefaults(0.0F, 12.0F/16F, 2.0F/16F, 180.0F, 180.0F, 0F, 1.0F);
            case "cosmetic_charm" ->
                    new SlotDefaults(0.0F, 6.0F/16F, -1.05F/16F, 180.0F, 180.0F, 0F, 1.0F);
            case "cosmetic_face" ->
                    new SlotDefaults(0.0F, -2.5F/16F, -2.05F/16F, 180.0F, 180.0F, 0F, 1.0F);
            case "cosmetic_hand" ->
                    new SlotDefaults(0.5F/16F, 9.0F/16F, 0.0F, 180.0F, 180.0F, 0F, 1.0F);
            case "cosmetic_necklace" ->
                    new SlotDefaults(0.0F, 6.0F/16F, -1.05F/16F, 180.0F, 180.0F, 0F, 1.0F);
            case "cosmetic_ring" ->
                    new SlotDefaults(0.5F/16F, 9.9F/16F, 0.0F, 180.0F, 180.0F, 0F, 0.33F);
            case "cosmetic_shoes" ->
                    new SlotDefaults(0.0F, 12.0F/16F, 0.0F, 180.0F, 180.0F, 0F, 1.05F);
            case "cosmetic_wrist" ->
                    new SlotDefaults(0.5F/16F, 9.0F/16F, 0.0F, 180.0F, 180.0F, 0F, 1.0F);
            case "cosmetic_dynamax_band" ->
                    new SlotDefaults(0.5F/16F, 9.0F/16F, -1.5F/16F, 180.0F, 180.0F, 0F, 0.5F);
            case "cosmetic_mega_bracelet" ->
                    new SlotDefaults(0.5F/16F, 9.0F/16F, 0.0F, 180.0F, 180.0F, 0F, 1.0F);
            case "cosmetic_tera_orb" ->
                    new SlotDefaults(-3.0F/16F, 5.0F/16F, -3.0F/16F, 180.0F, 180.0F, 0F, 1.0F);
            case "cosmetic_z_ring" ->
                    new SlotDefaults(0.5F/16F, 9.9F/16F, 0.0F, 180.0F, 180.0F, 0F, 0.33F);
            default ->
                    new SlotDefaults(0.0F, 0.0F, 0.0F, 0F, 0F, 0F, 1.0F);
        };
    }

    private static CosmeticDefinition getCosmeticDefinition(ItemStack stack) {
        String cosmeticId = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                .copyTag().getString("cosmetic");
        if (cosmeticId.isEmpty()) return null;
        ResourceLocation id = ResourceLocation.tryParse(cosmeticId);
        if (id == null) return null;
        return CosmeticDataLoader.getDefinition(id);
    }

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
        if (!(entityModel instanceof HumanoidModel<?> humanoidModel)) {
            return;
        }

        poseStack.pushPose();

        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        SlotDefaults defaults = getDefaults(itemId);
        CosmeticDefinition def = getCosmeticDefinition(stack);

        // Attach to correct body part
        switch (itemId) {
            case "cosmetic_hand", "cosmetic_ring", "cosmetic_wrist", "cosmetic_dynamax_band", "cosmetic_mega_bracelet", "cosmetic_z_ring" ->
                    humanoidModel.leftArm.translateAndRotate(poseStack);
            case "cosmetic_face", "cosmetic_hat" ->
                    humanoidModel.head.translateAndRotate(poseStack);

            case "cosmetic_anklet" ->
                    humanoidModel.rightLeg.translateAndRotate(poseStack);
            case "cosmetic_back", "cosmetic_belt", "cosmetic_cape", "cosmetic_charm", "cosmetic_necklace", "cosmetic_tera_orb" ->
                    humanoidModel.body.translateAndRotate(poseStack);
            default ->
                    humanoidModel.body.translateAndRotate(poseStack);
        }

        applyTransforms(poseStack, defaults, def);
        Minecraft.getInstance().getItemRenderer().renderStatic(
                reference.entity(), stack, ItemDisplayContext.NONE, false,
                poseStack, bufferSource, reference.entity().level(),
                packedLight, OverlayTexture.NO_OVERLAY, 0
        );
        poseStack.popPose();
    }

    private void renderPaired(ItemStack stack, SlotReference reference, PoseStack poseStack,
                              ModelPart left, ModelPart right, MultiBufferSource bufferSource,
                              int packedLight, SlotDefaults defaults, CosmeticDefinition def) {
        // Left
        poseStack.pushPose();
        left.translateAndRotate(poseStack);
        applyTransforms(poseStack, defaults, def);
        Minecraft.getInstance().getItemRenderer().renderStatic(
                reference.entity(), stack, ItemDisplayContext.NONE, false,
                poseStack, bufferSource, reference.entity().level(),
                packedLight, OverlayTexture.NO_OVERLAY, 0
        );
        poseStack.popPose();

        // Right
        poseStack.pushPose();
        right.translateAndRotate(poseStack);
        applyTransforms(poseStack, defaults, def);
        Minecraft.getInstance().getItemRenderer().renderStatic(
                reference.entity(), stack, ItemDisplayContext.NONE, false,
                poseStack, bufferSource, reference.entity().level(),
                packedLight, OverlayTexture.NO_OVERLAY, 0
        );
        poseStack.popPose();
    }

    private void applyTransforms(PoseStack poseStack, SlotDefaults defaults, CosmeticDefinition def) {
        // Apply default transforms
        poseStack.translate(defaults.transX, defaults.transY, defaults.transZ);
        poseStack.mulPose(Axis.XP.rotationDegrees(defaults.rotX));
        poseStack.mulPose(Axis.YP.rotationDegrees(defaults.rotY));
        poseStack.mulPose(Axis.ZP.rotationDegrees(defaults.rotZ));
        poseStack.scale(defaults.scale, defaults.scale, defaults.scale);

        // Datapack overrides
        if (def != null) {
            poseStack.translate(
                    def.translateX().orElse(0f),
                    -def.translateY().orElse(0f),
                    -def.translateZ().orElse(0f)
            );
            poseStack.mulPose(Axis.XP.rotationDegrees(def.rotateX().orElse(0f)));
            poseStack.mulPose(Axis.YP.rotationDegrees(-def.rotateY().orElse(0f)));
            poseStack.mulPose(Axis.ZP.rotationDegrees(-def.rotateZ().orElse(0f)));
            def.scale().ifPresent(s -> poseStack.scale(s, s, s));
        }
    }
}