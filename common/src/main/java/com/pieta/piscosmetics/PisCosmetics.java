package com.pieta.piscosmetics;

import com.pieta.piscosmetics.compat.accessories.AccessoriesCompat;
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

    /** Setup after items are registered, only touches optional mods when they're installed */
    public static void commonSetup() {
        if (Services.PLATFORM.isModLoaded("accessories")) AccessoriesCompat.init();
    }

    /** Sends all definitions to a player, on join and after /reload */
    public static void sendDefinitions(ServerPlayer player) {
        Services.PLATFORM.sendToPlayer(player, new CosmeticSyncPacket(CosmeticDataLoader.getAllDefinitions()));
    }
}
