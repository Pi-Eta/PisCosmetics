package com.pieta.piscosmetics.client.renderer;

import com.pieta.piscosmetics.client.MovementState;
import net.minecraft.world.entity.LivingEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.pieta.piscosmetics.api.CosmeticDefinition;
import com.pieta.piscosmetics.api.CosmeticSlotNames;
import com.pieta.piscosmetics.data.CosmeticDataLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * Draws one worn cosmetic on a humanoid model. Used for every place a cosmetic can be worn:
 * the vanilla armor slots (CosmeticArmorRenderMixin) and, when installed, Accessories slots
 * (compat/accessories/CosmeticAccessoryRenderer). It mentions no Accessories types, so it works without it.
 */
public final class CosmeticRenderer {

    private CosmeticRenderer() {}

    private record SlotDefaults(
            float transX, float transY, float transZ,
            float rotX, float rotY, float rotZ,
            float scale
    ) {}

    /** Offsets, rotation and scale per slot key (see CosmeticSlotNames). */
    private static SlotDefaults getDefaults(String slotKey) {
        return switch (slotKey) {
            case "hat" ->
                    new SlotDefaults(0.0F, -7.01F/16F, 0.0F, 180.0F, 180.0F, 0F, 1.0F);
            case "anklet" ->
                    new SlotDefaults(0.0F, 9.0F/16F, 0.0F, 180.0F, 180.0F, 0F, 1.0F);
            case "back" ->
                    new SlotDefaults(0.0F, 9.0F/16F, 2.0F/16F, 180.0F, 180.0F, 0F, 1.0F);
            case "belt" ->
                    new SlotDefaults(-1.0F/16F, 0.5F, 0.0F, 180.0F, 180.0F, 0F, 1.0F);
            case "cape" ->
                    new SlotDefaults(0.0F, 12.0F/16F, 2.0F/16F, 180.0F, 180.0F, 0F, 1.0F);
            case "charm" ->
                    new SlotDefaults(0.0F, 6.0F/16F, -1.05F/16F, 180.0F, 180.0F, 0F, 1.0F);
            case "face" ->
                    new SlotDefaults(0.0F, -2.5F/16F, -2.05F/16F, 180.0F, 180.0F, 0F, 1.0F);
            case "hand" ->
                    new SlotDefaults(0.5F/16F, 9.0F/16F, 0.0F, 180.0F, 180.0F, 0F, 1.0F);
            case "necklace" ->
                    new SlotDefaults(0.0F, 6.0F/16F, -1.05F/16F, 180.0F, 180.0F, 0F, 1.0F);
            case "ring" ->
                    new SlotDefaults(0.5F/16F, 9.9F/16F, 0.0F, 180.0F, 180.0F, 0F, 0.33F);
            case "shoes" ->
                    new SlotDefaults(0.0F, 12.0F/16F, 0.0F, 180.0F, 180.0F, 0F, 1.05F);
            case "wrist" ->
                    new SlotDefaults(0.5F/16F, 9.0F/16F, 0.0F, 180.0F, 180.0F, 0F, 1.0F);
            case "dynamax_slot" ->
                    new SlotDefaults(0.5F/16F, 9.0F/16F, -1.5F/16F, 180.0F, 180.0F, 0F, 0.5F);
            case "mega_slot" ->
                    new SlotDefaults(0.5F/16F, 9.0F/16F, 0.0F, 180.0F, 180.0F, 0F, 1.0F);
            case "tera_slot" ->
                    new SlotDefaults(-3.0F/16F, 5.0F/16F, -3.0F/16F, 180.0F, 180.0F, 0F, 1.0F);
            case "z_slot" ->
                    new SlotDefaults(0.5F/16F, 9.9F/16F, 0.0F, 180.0F, 180.0F, 0F, 0.33F);
            case "head" ->
                    new SlotDefaults(0.0F, -7.01F/16F, 0.0F, 180.0F, 180.0F, 0F, 1.0F);
            case "chest" ->
                    new SlotDefaults(0.0F, 9.0F/16F, 0.0F, 180.0F, 180.0F, 0F, 1.0F);
            case "legs" ->
                    new SlotDefaults(0.0F, 12.0F/16F, 0.0F, 180.0F, 180.0F, 0F, 1.0F);
            case "feet" ->
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

    public static void render(
            ItemStack stack,
            LivingEntity entity,
            PoseStack poseStack,
            HumanoidModel<?> humanoidModel,
            MultiBufferSource bufferSource,
            int packedLight
    ) {

        String slotKey = CosmeticSlotNames.slotKey(stack);
        SlotDefaults defaults = getDefaults(slotKey);
        CosmeticDefinition def = getCosmeticDefinition(stack);
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
        if (isPairedItem(slotKey)) {
            renderBothSides(stack, slotKey, poseStack, humanoidModel, defaults, def, bufferSource, packedLight, entity);
        } else {
            String attachPoint = (def != null && def.attach().isPresent())
                    ? def.attach().get()
                    : getDefaultAttachPoint(slotKey);

            poseStack.pushPose();
            attachToBodyPart(poseStack, humanoidModel, attachPoint);
            applyTransforms(poseStack, defaults, def);
            Minecraft.getInstance().getItemRenderer().renderStatic(
                    stack, ItemDisplayContext.NONE, packedLight, OverlayTexture.NO_OVERLAY,
                    poseStack, bufferSource, entity.level(), 0);
            poseStack.popPose();
        }
    }

    private static boolean isPairedItem(String slotKey) {
        return switch (slotKey) {
            case "shoes", "feet", "legs" -> true;
            default -> false;
        };
    }

    private static void renderBothSides(ItemStack stack, String slotKey, PoseStack poseStack, HumanoidModel<?> model,
                                 SlotDefaults defaults, CosmeticDefinition def,
                                 MultiBufferSource bufferSource, int packedLight, LivingEntity entity) {
        String leftAttach, rightAttach;
        if (slotKey.equals("shoes") || slotKey.equals("feet") || slotKey.equals("anklet") || slotKey.equals("legs")) {
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

    private static String getDefaultAttachPoint(String slotKey) {
        return switch (slotKey) {
            case "hand", "ring", "wrist",
                 "dynamax_slot", "mega_slot", "z_slot" -> "left_arm";
            case "face", "hat" -> "head";
            case "anklet" -> "right_leg";
            case "head" -> "head";
            case "chest" -> "body";
            case "legs" -> "right_leg";
            case "feet" -> "right_leg";
            default -> "body";
        };
    }

    private static void attachToBodyPart(PoseStack poseStack, HumanoidModel<?> model, String attachPoint) {
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

    private static void applyTransforms(PoseStack poseStack, SlotDefaults defaults, CosmeticDefinition def) {
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