package com.pieta.piscosmetics.client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.pieta.piscosmetics.item.CosmeticItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

import java.util.Optional;

public class CosmeticEmissiveLayer extends GeoRenderLayer<CosmeticItem> {

    public CosmeticEmissiveLayer(GeoItemRenderer<CosmeticItem> renderer) {
        super(renderer);
    }

    @Override
    public void render(
            PoseStack poseStack,
            CosmeticItem animatable,
            BakedGeoModel bakedModel,
            RenderType renderType,
            MultiBufferSource bufferSource,
            com.mojang.blaze3d.vertex.VertexConsumer buffer,
            float partialTick,
            int packedLight,
            int packedOverlay
    ) {

        ResourceLocation baseTexture =
                getRenderer().getTextureLocation(animatable);

        String path = baseTexture.getPath();

        if (path.endsWith(".png")) {
            path = path.substring(0, path.length() - 4);
        }

        ResourceLocation glowTexture =
                ResourceLocation.fromNamespaceAndPath(
                        baseTexture.getNamespace(),
                        path + "_emissive.png"
                );

        Optional<Resource> resource = Minecraft.getInstance()
                .getResourceManager()
                .getResource(glowTexture);

        if (resource.isEmpty()) {
            return;
        }

        RenderType glowRenderType = RenderType.eyes(glowTexture);

        getRenderer().reRender(
                bakedModel,
                poseStack,
                bufferSource,
                animatable,
                glowRenderType,
                bufferSource.getBuffer(glowRenderType),
                partialTick,
                0xF000F0,
                packedOverlay,
                0xFFFFFFFF
        );
    }
}