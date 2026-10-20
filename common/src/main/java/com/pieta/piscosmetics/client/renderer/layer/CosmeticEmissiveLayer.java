package com.pieta.piscosmetics.client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.pieta.piscosmetics.item.CosmeticItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

import java.util.HashMap;
import java.util.Map;

/**
 * Draws <texture>_emissive.png on top at full brightness, as strong as its alpha
 */
public class CosmeticEmissiveLayer extends GeoRenderLayer<CosmeticItem> {

    // Whether each texture has an emissive file, rechecked every few seconds so F3+T picks up new ones
    private static final long RECHECK_MS = 5000;
    private static final Map<ResourceLocation, Long> CHECKED_AT = new HashMap<>();
    private static final Map<ResourceLocation, Boolean> HAS_GLOW = new HashMap<>();

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

        if (!hasGlow(glowTexture)) return;

        // Full brightness, blended by alpha: transparent pixels don't glow, half-transparent ones glow half
        RenderType glowRenderType = RenderType.entityTranslucentEmissive(glowTexture);

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

    private static boolean hasGlow(ResourceLocation glowTexture) {
        long now = System.currentTimeMillis();
        Long checked = CHECKED_AT.get(glowTexture);
        if (checked == null || now - checked > RECHECK_MS) {
            HAS_GLOW.put(glowTexture, Minecraft.getInstance().getResourceManager().getResource(glowTexture).isPresent());
            CHECKED_AT.put(glowTexture, now);
        }
        return HAS_GLOW.get(glowTexture);
    }
}