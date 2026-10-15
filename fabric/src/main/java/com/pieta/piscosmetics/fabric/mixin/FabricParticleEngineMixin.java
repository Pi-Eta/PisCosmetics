package com.pieta.piscosmetics.fabric.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Draws particles with Fabric custom particle type
 */
@Mixin(ParticleEngine.class)
public abstract class FabricParticleEngineMixin {

    @Shadow @Final private static List<ParticleRenderType> RENDER_ORDER;
    @Shadow @Final private Map<ParticleRenderType, Queue<Particle>> particles;
    @Shadow @Final private TextureManager textureManager;

    @Inject(method = "render(Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;F)V",
            at = @At("TAIL"))
    private void piscosmetics$renderCustomTypes(LightTexture lightTexture, Camera camera, float partialTick,
                                                CallbackInfo ci) {
        boolean drewAny = false;
        for (Map.Entry<ParticleRenderType, Queue<Particle>> entry : this.particles.entrySet()) {
            ParticleRenderType type = entry.getKey();
            Queue<Particle> queue = entry.getValue();
            if (RENDER_ORDER.contains(type) || type == ParticleRenderType.NO_RENDER
                    || queue == null || queue.isEmpty()) {
                continue;
            }
            if (!drewAny) {
                // Vanilla will turn these off so have to turn them back on
                lightTexture.turnOnLightLayer();
                RenderSystem.enableDepthTest();
                drewAny = true;
            }
            RenderSystem.setShader(GameRenderer::getParticleShader);
            BufferBuilder builder = type.begin(Tesselator.getInstance(), this.textureManager);
            if (builder == null) continue;
            for (Particle particle : queue) {
                particle.render(builder, camera, partialTick);
            }
            MeshData mesh = builder.build();
            if (mesh != null) {
                BufferUploader.drawWithShader(mesh);
            }
        }
        if (drewAny) {
            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
            lightTexture.turnOffLightLayer();
        }
    }
}
