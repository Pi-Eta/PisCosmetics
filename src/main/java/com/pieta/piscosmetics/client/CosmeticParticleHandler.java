package com.pieta.piscosmetics.client;

import com.pieta.piscosmetics.api.CosmeticDefinition;
import com.pieta.piscosmetics.data.CosmeticDataLoader;
import io.wispforest.accessories.api.AccessoriesCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME)
public class CosmeticParticleHandler {

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

                var particleType = BuiltInRegistries.PARTICLE_TYPE.get(p.particleId());
                if (particleType == null) return;

                double x = player.getX() + p.offsetX().orElse(0f);
                double y = player.getEyeY() + p.offsetY().orElse(0f);
                double z = player.getZ() + p.offsetZ().orElse(0f);

                player.level().addParticle(
                        (net.minecraft.core.particles.ParticleOptions) particleType,
                        x + (player.getRandom().nextDouble() - 0.5) * p.spread(),
                        y,
                        z + (player.getRandom().nextDouble() - 0.5) * p.spread(),
                        0, 0.02, 0
                );
            });
        });
    }
}