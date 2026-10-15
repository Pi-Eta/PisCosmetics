package com.pieta.piscosmetics.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.pieta.piscosmetics.api.CosmeticDefinition;
import com.pieta.piscosmetics.data.CosmeticDataLoader;
import com.pieta.piscosmetics.item.CosmeticItem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {

    @Unique
    private static final String ICON_PREFIX = "textures/";
    @Unique
    private static final String ICON_SUFFIX = ".png";

    @Inject(
            method = "render(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/resources/model/BakedModel;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void piscosmetics$renderDirectTexture(
            ItemStack stack,
            ItemDisplayContext displayContext,
            boolean leftHand,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int combinedLight,
            int combinedOverlay,
            BakedModel model,
            CallbackInfo ci
    ) {
        // intercept GUI and GROUND rendering
        if (displayContext != ItemDisplayContext.GUI && displayContext != ItemDisplayContext.GROUND) {
            return;
        }

        if (!(stack.getItem() instanceof CosmeticItem)) {
            return;
        }

        try {
            String cosmeticId = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                    .copyTag().getString("cosmetic");

            if (cosmeticId.isEmpty()) return;

            ResourceLocation cosmeticRL = ResourceLocation.tryParse(cosmeticId);
            if (cosmeticRL == null) return;

            CosmeticDefinition definition = CosmeticDataLoader.getDefinition(cosmeticRL);
            if (definition == null || definition.icon().isEmpty()) return;

            ResourceLocation iconLocation = definition.icon().get();

            ResourceLocation textureLocation = ResourceLocation.fromNamespaceAndPath(
                    iconLocation.getNamespace(),
                    ICON_PREFIX + iconLocation.getPath() + ICON_SUFFIX
            );
            ci.cancel();

            if (displayContext == ItemDisplayContext.GROUND) {
                poseStack.translate(0, 0.3f, 0);
            }

            // render the texture directly
            renderIconTexture(poseStack, buffer, combinedLight, textureLocation);

        } catch (Exception e) {

        }
    }

    @Unique
    private void renderIconTexture(PoseStack poseStack, MultiBufferSource buffer, int light, ResourceLocation texture) {
        poseStack.pushPose();

        float scale = 0.85f;
        poseStack.scale(scale, scale, 1);

        poseStack.translate(-0.5f, -0.5f, 0);

        RenderSystem.disableCull();

        VertexConsumer vertexConsumer = buffer.getBuffer(net.minecraft.client.renderer.RenderType.text(texture));
        var pose = poseStack.last().pose();

        float uvMin = 0.01f;   // Slight inset to avoid edge pixels
        float uvMax = 0.99f;   // Slight inset to avoid edge pixels

        vertexConsumer.addVertex(pose, 0, 0, 0).setUv(uvMin, uvMin).setLight(light).setColor(255, 255, 255, 255);
        vertexConsumer.addVertex(pose, 1, 0, 0).setUv(uvMax, uvMin).setLight(light).setColor(255, 255, 255, 255);
        vertexConsumer.addVertex(pose, 1, 1, 0).setUv(uvMax, uvMax).setLight(light).setColor(255, 255, 255, 255);
        vertexConsumer.addVertex(pose, 0, 1, 0).setUv(uvMin, uvMax).setLight(light).setColor(255, 255, 255, 255);

        vertexConsumer.addVertex(pose, 0, 1, 0).setUv(uvMin, uvMax).setLight(light).setColor(255, 255, 255, 255);
        vertexConsumer.addVertex(pose, 1, 1, 0).setUv(uvMax, uvMax).setLight(light).setColor(255, 255, 255, 255);
        vertexConsumer.addVertex(pose, 1, 0, 0).setUv(uvMax, uvMin).setLight(light).setColor(255, 255, 255, 255);
        vertexConsumer.addVertex(pose, 0, 0, 0).setUv(uvMin, uvMin).setLight(light).setColor(255, 255, 255, 255);

        RenderSystem.enableCull();

        poseStack.popPose();
    }
}