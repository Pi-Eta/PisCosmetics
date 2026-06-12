package com.pieta.piscosmetics.client.renderer;

import com.pieta.piscosmetics.client.MovementState;
import net.minecraft.world.entity.LivingEntity;
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
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

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
            case "cosmetic_head" ->
                    new SlotDefaults(0.0F, -7.01F/16F, 0.0F, 180.0F, 180.0F, 0F, 1.0F);
            case "cosmetic_chest" ->
                    new SlotDefaults(0.0F, 9.0F/16F, 0.0F, 180.0F, 180.0F, 0F, 1.0F);
            case "cosmetic_legs" ->
                    new SlotDefaults(0.0F, 12.0F/16F, 0.0F, 180.0F, 180.0F, 0F, 1.0F);
            case "cosmetic_feet" ->
                    new SlotDefaults(0.0F, 12.0F/16F, 0.0F, 180.0F, 180.0F, 0F, 1.05F);
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
        if (!(entityModel instanceof HumanoidModel<?> humanoidModel)) return;

        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        SlotDefaults defaults = getDefaults(itemId);
        CosmeticDefinition def = getCosmeticDefinition(stack);
        LivingEntity entity = reference.entity();
        MovementState.State state = MovementState.get(entity.getUUID());

        // Gliding override
        if (state.gliding) {
            poseStack.pushPose();
            humanoidModel.body.translateAndRotate(poseStack);
            applyTransforms(poseStack, defaults, def);
            Minecraft.getInstance().getItemRenderer().renderStatic(
                    stack, ItemDisplayContext.NONE, packedLight, OverlayTexture.NO_OVERLAY,
                    poseStack, bufferSource, entity.level(), 0);
            poseStack.popPose();
            return;
        }

        // Check if this item should render on both sides
        if (isPairedItem(itemId)) {
            renderBothSides(stack, poseStack, humanoidModel, defaults, def, bufferSource, packedLight, entity);
        } else {
            String attachPoint = (def != null && def.attach().isPresent())
                    ? def.attach().get()
                    : getDefaultAttachPoint(itemId);

            poseStack.pushPose();
            attachToBodyPart(poseStack, humanoidModel, attachPoint);
            applyTransforms(poseStack, defaults, def);
            Minecraft.getInstance().getItemRenderer().renderStatic(
                    stack, ItemDisplayContext.NONE, packedLight, OverlayTexture.NO_OVERLAY,
                    poseStack, bufferSource, entity.level(), 0);
            poseStack.popPose();
        }
    }

    private boolean isPairedItem(String itemId) {
        return switch (itemId) {
            case "cosmetic_shoes", "cosmetic_feet", "cosmetic_legs" -> true;
            default -> false;
        };
    }

    private void renderBothSides(ItemStack stack, PoseStack poseStack, HumanoidModel<?> model,
                                 SlotDefaults defaults, CosmeticDefinition def,
                                 MultiBufferSource bufferSource, int packedLight, LivingEntity entity) {
        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();

        String leftAttach, rightAttach;
        if (itemId.contains("shoes") || itemId.contains("feet") || itemId.contains("anklet") || itemId.contains("legs")) {
            leftAttach = "left_leg";
            rightAttach = "right_leg";
        } else {
            leftAttach = "left_arm";
            rightAttach = "right_arm";
        }

        // Left side
        poseStack.pushPose();
        attachToBodyPart(poseStack, model, leftAttach);
        poseStack.scale(-1, 1, 1); // Mirror X only
        applyTransforms(poseStack, defaults, def);
        Minecraft.getInstance().getItemRenderer().renderStatic(
                stack, ItemDisplayContext.NONE, packedLight, OverlayTexture.NO_OVERLAY,
                poseStack, bufferSource, entity.level(), 0);
        poseStack.popPose();

        // Right side
        poseStack.pushPose();
        attachToBodyPart(poseStack, model, rightAttach);
        applyTransforms(poseStack, defaults, def);
        Minecraft.getInstance().getItemRenderer().renderStatic(
                stack, ItemDisplayContext.NONE, packedLight, OverlayTexture.NO_OVERLAY,
                poseStack, bufferSource, entity.level(), 0);
        poseStack.popPose();
    }

    private String getDefaultAttachPoint(String itemId) {
        return switch (itemId) {
            case "cosmetic_hand", "cosmetic_ring", "cosmetic_wrist",
                 "cosmetic_dynamax_band", "cosmetic_mega_bracelet", "cosmetic_z_ring" -> "left_arm";
            case "cosmetic_face", "cosmetic_hat" -> "head";
            case "cosmetic_anklet" -> "right_leg";
            case "cosmetic_head" -> "head";
            case "cosmetic_chest" -> "body";
            case "cosmetic_legs" -> "right_leg";
            case "cosmetic_feet" -> "right_leg";
            default -> "body";
        };
    }

    private void attachToBodyPart(PoseStack poseStack, HumanoidModel<?> model, String attachPoint) {
        switch (attachPoint.toLowerCase()) {
            case "head" -> model.head.translateAndRotate(poseStack);
            case "body", "chest" -> model.body.translateAndRotate(poseStack);
            case "left_arm" -> model.leftArm.translateAndRotate(poseStack);
            case "right_arm" -> model.rightArm.translateAndRotate(poseStack);
            case "left_leg" -> model.leftLeg.translateAndRotate(poseStack);
            case "right_leg" -> model.rightLeg.translateAndRotate(poseStack);
            default -> model.body.translateAndRotate(poseStack);
        }
    }

    private void applyTransforms(PoseStack poseStack, SlotDefaults defaults, CosmeticDefinition def) {
        // Apply defaults FIRST (as-is, no negation)
        poseStack.translate(defaults.transX, defaults.transY, defaults.transZ);
        poseStack.mulPose(Axis.XP.rotationDegrees(defaults.rotX));
        poseStack.mulPose(Axis.YP.rotationDegrees(defaults.rotY));
        poseStack.mulPose(Axis.ZP.rotationDegrees(defaults.rotZ));
        poseStack.scale(defaults.scale, defaults.scale, defaults.scale);

        if (def != null) {
            poseStack.translate(
                    def.translateX().orElse(0f),
                    def.translateY().orElse(0f),   // Flip Y (user's positive = up)
                    def.translateZ().orElse(0f)    // Flip Z (user's positive = forward)
            );
            poseStack.mulPose(Axis.XP.rotationDegrees(def.rotateX().orElse(0f)));
            poseStack.mulPose(Axis.YP.rotationDegrees(def.rotateY().orElse(0f)));  // Flip Y rotation
            poseStack.mulPose(Axis.ZP.rotationDegrees(def.rotateZ().orElse(0f)));  // Flip Z rotation
            def.scale().ifPresent(s -> poseStack.scale(s, s, s));  // Override scale
        }
    }
}