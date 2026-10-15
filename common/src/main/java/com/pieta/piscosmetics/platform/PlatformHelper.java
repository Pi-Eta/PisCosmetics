package com.pieta.piscosmetics.platform;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

/**
 * Good for seeing if trinkets/accessories/showdown/genesis is installed
 */
public interface PlatformHelper {

    /** True if a mod with this ID is installed */
    boolean isModLoaded(String modId);

    /** Send a packet to one player */
    void sendToPlayer(ServerPlayer player, CustomPacketPayload payload);

    /** Send a packet to every player who can see this player, plus the player themselves */
    void sendToTrackingAndSelf(ServerPlayer player, CustomPacketPayload payload);
}
