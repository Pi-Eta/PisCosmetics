package com.pieta.piscosmetics.client;

import com.pieta.piscosmetics.api.ParticleEmitter;

import java.util.*;

/**
 * Contains the last particles sent by the server, meant to load as client side particles
 */
public class ClientParticleData {
    private static final Map<Integer, List<ParticleEmitter>> PLAYER_PARTICLES = new HashMap<>();

    public static void setParticleEmitters(int entityId, List<ParticleEmitter> emitters) {
        if (emitters.isEmpty()) {
            PLAYER_PARTICLES.remove(entityId);
        } else {
            PLAYER_PARTICLES.put(entityId, new ArrayList<>(emitters));
        }
    }

    public static List<ParticleEmitter> getParticleEmitters(int entityId) {
        return PLAYER_PARTICLES.getOrDefault(entityId, List.of());
    }

    public static void removePlayer(int entityId) {
        PLAYER_PARTICLES.remove(entityId);
    }

    public static void clearAll() {
        PLAYER_PARTICLES.clear();
    }
}