package com.pieta.piscosmetics.fabric;

import com.pieta.piscosmetics.network.CosmeticSyncPacket;
import com.pieta.piscosmetics.network.ParticleSyncPacket;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

/**
 * Defines packet types
 */
public final class FabricNetworking {

    private FabricNetworking() {}

    public static void registerCommon() {
        PayloadTypeRegistry.playS2C().register(CosmeticSyncPacket.TYPE, CosmeticSyncPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(ParticleSyncPacket.TYPE, ParticleSyncPacket.STREAM_CODEC);
    }
}
