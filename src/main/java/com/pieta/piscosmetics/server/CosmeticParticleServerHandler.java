package com.pieta.piscosmetics.server;

import com.pieta.piscosmetics.api.CosmeticDefinition;
import com.pieta.piscosmetics.api.ParticleEmitter;
import com.pieta.piscosmetics.data.CosmeticDataLoader;
import com.pieta.piscosmetics.network.ParticleSyncPacket;
import io.wispforest.accessories.api.AccessoriesCapability;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

@EventBusSubscriber
public class CosmeticParticleServerHandler {
    private static final Map<UUID, List<ParticleEmitter>> LAST_EMITTERS = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        List<ParticleEmitter> currentEmitters = getEmittersFromCosmetics(player);

        List<ParticleEmitter> lastEmitters = LAST_EMITTERS.getOrDefault(player.getUUID(), List.of());

        if (!currentEmitters.equals(lastEmitters)) {
            LAST_EMITTERS.put(player.getUUID(), new ArrayList<>(currentEmitters));

            PacketDistributor.sendToPlayersTrackingEntityAndSelf(
                    player,
                    new ParticleSyncPacket(player.getId(), currentEmitters)
            );
        }
    }

    private static List<ParticleEmitter> getEmittersFromCosmetics(ServerPlayer player) {
        List<ParticleEmitter> emitters = new ArrayList<>();
        var capability = AccessoriesCapability.get(player);
        if (capability == null) return emitters;

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
            if (def != null && !def.particleEmitters().isEmpty()) {
                emitters.addAll(def.particleEmitters());
            }
        });

        return emitters;
    }
}