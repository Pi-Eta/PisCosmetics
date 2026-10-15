package com.pieta.piscosmetics.fabric.client;

import com.pieta.piscosmetics.network.CosmeticSyncPacket;
import com.pieta.piscosmetics.network.ParticleSyncPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

/**
 * How the client receives packets from the server
 */
public final class FabricClientNetworking {

    private FabricClientNetworking() {}

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(CosmeticSyncPacket.TYPE,
                (packet, context) -> CosmeticSyncPacket.handle(packet));
        ClientPlayNetworking.registerGlobalReceiver(ParticleSyncPacket.TYPE,
                (packet, context) -> ParticleSyncPacket.handle(packet));
    }
}
