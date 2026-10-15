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
import com.pieta.piscosmetics.platform.Services;

import java.util.*;

/**
 * Makes particle visible to other players on a server by telling you what particles they have
 */
public class CosmeticParticleServerHandler {
    private static final Map<UUID, List<ParticleEmitter>> LAST_EMITTERS = new HashMap<>();

    public static void onPlayerTick(ServerPlayer player) {
        List<ParticleEmitter> currentEmitters = getEmittersFromCosmetics(player);

        List<ParticleEmitter> lastEmitters = LAST_EMITTERS.getOrDefault(player.getUUID(), List.of());

        if (!currentEmitters.equals(lastEmitters)) {
            LAST_EMITTERS.put(player.getUUID(), new ArrayList<>(currentEmitters));

            Services.PLATFORM.sendToTrackingAndSelf(player, new ParticleSyncPacket(player.getId(), currentEmitters));
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