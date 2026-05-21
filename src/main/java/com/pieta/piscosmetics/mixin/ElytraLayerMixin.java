package com.pieta.piscosmetics.mixin;

import com.pieta.piscosmetics.api.CosmeticDefinition;
import com.pieta.piscosmetics.data.CosmeticDataLoader;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.slot.SlotEntryReference;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ElytraLayer.class)
public class ElytraLayerMixin<T extends LivingEntity, M extends EntityModel<T>> {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void piscosmetics$cancelElytraRender(
            com.mojang.blaze3d.vertex.PoseStack poseStack,
            net.minecraft.client.renderer.MultiBufferSource buffer,
            int packedLight,
            T livingEntity,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float ageInTicks,
            float netHeadYaw,
            float headPitch,
            CallbackInfo ci
    ) {
        if (livingEntity instanceof Player player) {
            // Check Accessories slots
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
                            if (def != null && def.elytra().orElse(false)) {
                                ci.cancel();
                                return;
                            }
                        }
                    }
                }
            }

            // Also check vanilla armor slots (in case someone puts it there)
            for (ItemStack stack : player.getArmorSlots()) {
                String cosmeticId = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                        .copyTag().getString("cosmetic");
                if (!cosmeticId.isEmpty()) {
                    ResourceLocation id = ResourceLocation.tryParse(cosmeticId);
                    if (id != null) {
                        CosmeticDefinition def = CosmeticDataLoader.getDefinition(id);
                        if (def != null && def.elytra().orElse(false)) {
                            ci.cancel();
                            return;
                        }
                    }
                }
            }
        }
    }
}