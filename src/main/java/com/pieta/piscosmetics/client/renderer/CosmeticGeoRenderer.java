package com.pieta.piscosmetics.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.pieta.piscosmetics.item.CosmeticItem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import com.pieta.piscosmetics.api.CosmeticDefinition;
import com.pieta.piscosmetics.data.CosmeticDataLoader;

public class CosmeticGeoRenderer extends GeoItemRenderer<CosmeticItem> {

    public CosmeticGeoRenderer(GeoModel<CosmeticItem> model) {
        super(model);
    }

    @Override
    public ResourceLocation getTextureLocation(CosmeticItem animatable) {
        return getGeoModel().getTextureResource(animatable);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context,
                             PoseStack poseStack, MultiBufferSource buffer,
                             int light, int overlay) {

        // Apply scale for hand-held contexts only (first-person and third-person)
        if (context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND ||
                context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND ||
                context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND ||
                context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND) {

            // Get scale from cosmetic definition if available, or use default
            CosmeticDefinition def = getCurrentDefinition(stack);
            float scale = def != null ? def.scale().orElse(1.0f) : 1.0f;
            poseStack.scale(scale, scale, scale);
        }

        super.renderByItem(stack, context, poseStack, buffer, light, overlay);
    }

    private CosmeticDefinition getCurrentDefinition(ItemStack stack) {
        try {
            String cosmeticId = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                    .copyTag().getString("cosmetic");
            if (cosmeticId.isEmpty()) return null;
            ResourceLocation id = ResourceLocation.tryParse(cosmeticId);
            if (id == null) return null;
            return CosmeticDataLoader.getDefinition(id);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public RenderType getRenderType(CosmeticItem animatable, ResourceLocation texture,
                                    MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }
}