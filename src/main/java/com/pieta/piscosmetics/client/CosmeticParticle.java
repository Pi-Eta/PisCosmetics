package com.pieta.piscosmetics.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CosmeticParticle extends Particle {
    private final ResourceLocation texture;
    private final float size;

    private static final Map<ResourceLocation, ParticleRenderType> RENDER_TYPES = new ConcurrentHashMap<>();

    protected CosmeticParticle(ClientLevel level, double x, double y, double z,
                               CosmeticParticleOptions options) {
        super(level, x, y, z);
        this.texture = options.texture();
        this.lifetime = options.lifetime();
        this.hasPhysics = false;
        this.alpha = 1.0f;
        this.size = options.size();
    }

    private ParticleRenderType getOrCreateRenderType() {
        return RENDER_TYPES.computeIfAbsent(texture, tex -> new ParticleRenderType() {
            @Override
            public BufferBuilder begin(Tesselator tesselator, TextureManager textureManager) {
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.depthMask(true);
                RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
                RenderSystem.setShaderTexture(0, tex);
                return tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            }

            @Override
            public String toString() {
                return "piscosmetics:custom";
            }
        });
    }

    @Override
    public ParticleRenderType getRenderType() {
        return getOrCreateRenderType();
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.depthMask(true);
        if (this.texture == null) return;

        float px = (float)(this.xo + (this.x - this.xo) * partialTicks - camera.getPosition().x);
        float py = (float)(this.yo + (this.y - this.yo) * partialTicks - camera.getPosition().y);
        float pz = (float)(this.zo + (this.z - this.zo) * partialTicks - camera.getPosition().z);

        Quaternionf rotation = new Quaternionf(camera.rotation());

        Vector3f[] vertices = new Vector3f[] {
                new Vector3f(-size, -size, 0),
                new Vector3f(-size, size, 0),
                new Vector3f(size, size, 0),
                new Vector3f(size, -size, 0)
        };

        for (int i = 0; i < 4; i++) {
            vertices[i].rotate(rotation);
            vertices[i].add(px, py, pz);
        }

        buffer.addVertex(vertices[0].x(), vertices[0].y(), vertices[0].z()).setUv(0, 1).setColor(1f, 1f, 1f, this.alpha);
        buffer.addVertex(vertices[1].x(), vertices[1].y(), vertices[1].z()).setUv(0, 0).setColor(1f, 1f, 1f, this.alpha);
        buffer.addVertex(vertices[2].x(), vertices[2].y(), vertices[2].z()).setUv(1, 0).setColor(1f, 1f, 1f, this.alpha);
        buffer.addVertex(vertices[3].x(), vertices[3].y(), vertices[3].z()).setUv(1, 1).setColor(1f, 1f, 1f, this.alpha);
    }

    public static class Provider implements ParticleProvider<CosmeticParticleOptions> {
        @Override
        public Particle createParticle(CosmeticParticleOptions options, ClientLevel level,
                                       double x, double y, double z,
                                       double vx, double vy, double vz) {
            return new CosmeticParticle(level, x, y, z, options);
        }
    }
}