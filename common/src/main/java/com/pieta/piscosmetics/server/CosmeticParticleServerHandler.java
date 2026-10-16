package com.pieta.piscosmetics.server;

import com.pieta.piscosmetics.api.CosmeticDefinition;
import com.pieta.piscosmetics.api.CosmeticSlots;
import com.pieta.piscosmetics.api.ParticleEmitter;
import com.pieta.piscosmetics.network.ParticleSyncPacket;
import net.minecraft.server.level.ServerPlayer;
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
        for (CosmeticDefinition def : CosmeticSlots.getWorn(player)) {
            emitters.addAll(def.particleEmitters());
        }
        return emitters;
    }
}
