package com.pieta.piscosmetics.neoforge;

import com.pieta.piscosmetics.network.CosmeticSyncPacket;
import com.pieta.piscosmetics.network.ParticleSyncPacket;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Packet type registration for NeoForge
 */
public final class NeoForgeNetworking {

    private NeoForgeNetworking() {}

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(CosmeticSyncPacket.TYPE, CosmeticSyncPacket.STREAM_CODEC,
                (packet, context) -> context.enqueueWork(() -> CosmeticSyncPacket.handle(packet)));
        registrar.playToClient(ParticleSyncPacket.TYPE, ParticleSyncPacket.STREAM_CODEC,
                (packet, context) -> context.enqueueWork(() -> ParticleSyncPacket.handle(packet)));
    }
}
