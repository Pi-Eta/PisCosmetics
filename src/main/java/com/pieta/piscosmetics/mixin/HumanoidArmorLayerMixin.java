package com.pieta.piscosmetics.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.pieta.piscosmetics.api.CosmeticDefinition;
import com.pieta.piscosmetics.data.CosmeticDataLoader;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.slot.SlotEntryReference;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
public class HumanoidArmorLayerMixin {

    @Inject(method = "renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/model/HumanoidModel;FFFFFF)V",
            at = @At("HEAD"), cancellable = true)
    private void piscosmetics$cancelArmorPiece(PoseStack poseStack, MultiBufferSource bufferSource, LivingEntity livingEntity,
                                               EquipmentSlot slot, int packedLight, HumanoidModel model,
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
        if (slotName == null) return;

        if (isSlotHidden(player, slotName)) {
            ci.cancel();
        }
    }

    private static boolean isSlotHidden(Player player, String slotName) {
        AccessoriesCapability capability = AccessoriesCapability.get(player);
        if (capability != null) {
            for (SlotEntryReference reference : capability.getAllEquipped()) {
                ItemStack stack = reference.stack();
                String cosmeticId = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                        .copyTag().getString("cosmetic");
                if (!cosmeticId.isEmpty()) {
                    ResourceLocation id = ResourceLocation.tryParse(cosmeticId);
                    if (id != null) {
                        CosmeticDefinition def = CosmeticDataLoader.getDefinition(id);
                        if (def != null) {
                            for (String hidden : def.hideArmor()) {
                                if (hidden.equals(slotName) || hidden.equals("all")) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
}