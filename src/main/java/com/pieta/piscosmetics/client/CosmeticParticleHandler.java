package com.pieta.piscosmetics.client;

import com.pieta.piscosmetics.ModParticles;
import com.pieta.piscosmetics.api.ParticleEmitter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class CosmeticParticleHandler {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.isPaused()) return;

        for (Player player : mc.level.players()) {
            var emitters = ClientParticleData.getParticleEmitters(player.getId());
            if (emitters.isEmpty()) continue;

            for (ParticleEmitter emitter : emitters) {
                if (player.tickCount % emitter.rate() != 0) continue;
                if (!matchesTrigger(player, emitter.trigger())) continue;

                for (int i = 0; i < emitter.count(); i++) {
                    spawnParticle(player, emitter);
                }
            }
        }
    }

    private static boolean matchesTrigger(Player player, String trigger) {
        return switch (trigger) {
            case "always", "idle" -> true;
            case "walking" -> player.walkDist - player.walkDistO > 0.01f;
            case "sprinting" -> player.isSprinting();
            case "jumping" -> !player.onGround() && player.getDeltaMovement().y > 0;
            case "falling" -> !player.onGround() && player.getDeltaMovement().y < -0.1;
            case "gliding" -> player.isFallFlying();
            case "sneaking" -> player.isCrouching();
            case "swimming" -> player.isSwimming();
            case "on_ground" -> player.onGround();
            case "in_air" -> !player.onGround();
            case "hurt" -> player.hurtTime > 0;
            case "on_fire" -> player.isOnFire();
            default -> true;
        };
    }

    private static void spawnParticle(Player player, ParticleEmitter emitter) {
        ParticleType<?> particleType = BuiltInRegistries.PARTICLE_TYPE.get(emitter.particleId());
        if (particleType == null) return;

        double x = player.getX() + emitter.offsetX().orElse(0.0);
        double y = player.getEyeY() + emitter.offsetY().orElse(0.0);
        double z = player.getZ() + emitter.offsetZ().orElse(0.0);

        x += (player.getRandom().nextDouble() - 0.5) * emitter.spread();
        z += (player.getRandom().nextDouble() - 0.5) * emitter.spread();

        ParticleOptions options = createParticleOptions(particleType, emitter);
        if (options != null) {
            player.level().addParticle(options, x, y, z, 0, 0.02, 0);
        }
    }

    private static ParticleOptions createParticleOptions(ParticleType<?> type, ParticleEmitter emitter) {
        String particleName = emitter.particleId().toString();

        if (type == ModParticles.COSMETIC_PARTICLE.get()) {
            ResourceLocation texture = emitter.texture().orElse(
                    ResourceLocation.fromNamespaceAndPath("piscosmetics", "cosmetic_particle"));
            ResourceLocation fullTexture = ResourceLocation.fromNamespaceAndPath(
                    texture.getNamespace(),
                    "textures/particle/" + texture.getPath() + ".png"
            );
            float size = emitter.customSize().orElse(0.25f);
            int lifetime = emitter.lifetime().orElse(30);
            return new CosmeticParticleOptions(fullTexture, size, lifetime);
        }

        if (type == ParticleTypes.ENTITY_EFFECT && particleName.equals("minecraft:entity_effect")) {
            String color = emitter.color().orElse("#FFFFFF");
            int rgb = parseColor(color);
            float r = ((rgb >> 16) & 0xFF) / 255.0f;
            float g = ((rgb >> 8) & 0xFF) / 255.0f;
            float b = (rgb & 0xFF) / 255.0f;
            return ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, r, g, b);
        }

        if (type instanceof SimpleParticleType simpleType) {
            return simpleType;
        }

        return null;
    }

    private static int parseColor(String s) {
        s = s.trim();
        if (s.startsWith("#")) s = s.substring(1);
        if (s.startsWith("0x") || s.startsWith("0X")) s = s.substring(2);
        try {
            return (int) (Long.parseLong(s, 16) & 0xFFFFFFL);
        } catch (NumberFormatException e) {
            return 0xFFFFFF;
        }
    }
}