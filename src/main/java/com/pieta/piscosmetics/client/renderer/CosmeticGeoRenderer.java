package com.pieta.piscosmetics.client.renderer;

import com.pieta.piscosmetics.item.CosmeticItem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class CosmeticGeoRenderer extends GeoItemRenderer<CosmeticItem> {

    public CosmeticGeoRenderer(GeoModel<CosmeticItem> model) {
        super(model);
    }

    @Override
    public ResourceLocation getTextureLocation(CosmeticItem animatable) {
        return getGeoModel().getTextureResource(animatable);
    }

    @Override
    public RenderType getRenderType(CosmeticItem animatable, ResourceLocation texture,
                                    MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }
}