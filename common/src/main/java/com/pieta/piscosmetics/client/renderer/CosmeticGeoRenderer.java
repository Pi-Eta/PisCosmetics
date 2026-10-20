package com.pieta.piscosmetics.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.pieta.piscosmetics.client.renderer.layer.CosmeticEmissiveLayer;
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
import com.pieta.piscosmetics.api.CosmeticSlotNames;
import com.pieta.piscosmetics.data.CosmeticDataLoader;
import com.pieta.piscosmetics.item.CosmeticHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import software.bernie.geckolib.model.DefaultedItemGeoModel;

public class CosmeticGeoRenderer extends GeoItemRenderer<CosmeticItem> {

    public CosmeticGeoRenderer(GeoModel<CosmeticItem> model) {
        super(model);
    }

    /**
     * Builds the renderer for  cosmetic items, now without initializeClient
     */
    public static CosmeticGeoRenderer create(CosmeticItem item) {
        CosmeticGeoRenderer[] holder = new CosmeticGeoRenderer[1];
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
        holder[0] = new CosmeticGeoRenderer(new DefaultedItemGeoModel<CosmeticItem>(itemId) {
            private CosmeticDefinition getDef() {
                ItemStack stack = holder[0].getCurrentItemStack();
                return CosmeticHelper.getDefinition(stack);
            }

            /**
             * The item name the default model, texture and animation are named after. For the universal
             * item that's the original item for its slot, so a "wrist" cosmetic without its own model
             * shows cosmetic_wrist's.
             */
            private String defaultsName() {
                ItemStack stack = holder[0].getCurrentItemStack();
                if (stack != null && CosmeticSlotNames.isUniversal(stack)) {
                    return CosmeticSlotNames.legacyItemFor(CosmeticSlotNames.slotKey(stack));
                }
                return itemId.getPath();
            }

            private ResourceLocation defaultPath(String folder, String suffix) {
                return ResourceLocation.fromNamespaceAndPath(itemId.getNamespace(), folder + defaultsName() + suffix);
            }

            @Override
            public ResourceLocation getModelResource(CosmeticItem animatable) {
                return CosmeticHelper.getModelResource(animatable, getDef(), defaultPath("geo/item/", ".geo.json"));
            }

            @Override
            public ResourceLocation getTextureResource(CosmeticItem animatable) {
                return CosmeticHelper.getTextureResource(animatable, getDef(), defaultPath("textures/item/", ".png"));
            }

            @Override
            public ResourceLocation getAnimationResource(CosmeticItem animatable) {
                return CosmeticHelper.getAnimationResource(animatable, getDef(), defaultPath("animations/", ".animation.json"));
            }
        });
        // Glow from <texture>_emissive.png
        holder[0].addRenderLayer(new CosmeticEmissiveLayer(holder[0]));
        return holder[0];
    }

    /**
     * Stacks without a GeckoLib ID share one animation state, so cosmetics on the same item would play each
     * other's animations. Gives each cosmetic its own, negative so it can't clash with real IDs
     */
    @Override
    public long getInstanceId(CosmeticItem animatable) {
        long id = super.getInstanceId(animatable);
        if (id != Long.MAX_VALUE) return id;
        ResourceLocation cosmeticId = CosmeticHelper.getCosmeticId(getCurrentItemStack());
        return cosmeticId == null ? id : -1L - (cosmeticId.toString().hashCode() & 0xFFFFFFFFL);
    }

    @Override
    public ResourceLocation getTextureLocation(CosmeticItem animatable) {
        return getGeoModel().getTextureResource(animatable);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context,
                             PoseStack poseStack, MultiBufferSource buffer,
                             int light, int overlay) {

        // first-person and third-person
        if (context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND ||
                context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND ||
                context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND ||
                context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND) {

            // get scale from cosmetic definition if available
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