package com.pieta.piscosmetics;

import com.pieta.piscosmetics.data.CosmeticDataLoader;
import com.pieta.piscosmetics.network.CosmeticSyncPacket;
import com.pieta.piscosmetics.platform.Services;
import net.minecraft.server.level.ServerPlayer;

/**
 * Shared functions independent of loaders
 */
public final class PisCosmetics {
    public static final String MODID = "piscosmetics";

    private PisCosmetics() {}

    /** Send definitions to new player joining server */
    public static void onPlayerJoin(ServerPlayer player) {
        Services.PLATFORM.sendToPlayer(player, new CosmeticSyncPacket(CosmeticDataLoader.getAllDefinitions()));
    }
}
