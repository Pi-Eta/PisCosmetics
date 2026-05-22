package com.pieta.piscosmetics.client;

import com.pieta.piscosmetics.api.CosmeticDefinition;
import com.pieta.piscosmetics.data.CosmeticDataLoader;
import io.wispforest.accessories.api.AccessoriesCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class CosmeticParticleHandler_d {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        AccessoriesCapability capability = AccessoriesCapability.get(player);
        if (capability == null) return;

        capability.getAllEquipped().forEach(entry -> {
            if (entry.stack().isEmpty()) return;

            String cosmeticId = entry.stack()
                    .getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                    .copyTag()
                    .getString("cosmetic");
            if (cosmeticId.isEmpty()) return;

            ResourceLocation id = ResourceLocation.tryParse(cosmeticId);
            if (id == null) return;

            CosmeticDefinition def = CosmeticDataLoader.getDefinition(id);
            if (def == null) return;

            def.particles().ifPresent(p -> {
                if (player.tickCount % p.rate() != 0) return;

                ParticleType<?> particleType = BuiltInRegistries.PARTICLE_TYPE.get(p.particleId());
                if (particleType == null) return;

                double x = player.getX() + p.offsetX().orElse(0f);
                double y = player.getEyeY() + p.offsetY().orElse(0f);
                double z = player.getZ() + p.offsetZ().orElse(0f);

                ParticleOptions particleOptions = null;
                String particleName = p.particleId().toString();

                // Handle entity_effect (colored particles)
                if (particleName.equals("minecraft:entity_effect")) {
                    // Use color from particle settings or default white
                    String color = p.color().orElse("#FFFFFF");
                    int rgb = parseColor(color);
                    // Change from 0-255 to 0-1 range
                    float r = ((rgb >> 16) & 0xFF) / 255.0f;
                    float g = ((rgb >> 8) & 0xFF) / 255.0f;
                    float b = (rgb & 0xFF) / 255.0f;
                    particleOptions = ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, r, g, b);  // 1.0f is scale
                }
                // Handle simple particles
                else if (particleType instanceof SimpleParticleType simpleType) {
                    particleOptions = simpleType;
                }

                if (particleOptions != null) {
                    player.level().addParticle(
                            particleOptions,
                            x + (player.getRandom().nextDouble() - 0.5) * p.spread(),
                            y,
                            z + (player.getRandom().nextDouble() - 0.5) * p.spread(),
                            0, 0.02, 0
                    );
                }
            });
        });
    }

    private static int parseColor(String s) {
        s = s.trim();
        if (s.startsWith("#")) s = s.substring(1);
        if (s.startsWith("0x") || s.startsWith("0X")) s = s.substring(2);
        try {
            return (int) (Long.parseLong(s, 16) & 0xFFFFFFL);
        } catch (NumberFormatException e) {
            return 0xFFFFFF; // Default white
        }
    }
}