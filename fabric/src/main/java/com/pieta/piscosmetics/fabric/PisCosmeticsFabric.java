package com.pieta.piscosmetics.fabric;

import com.pieta.piscosmetics.ModItems;
import com.pieta.piscosmetics.ModParticles;
import com.pieta.piscosmetics.PisCosmetics;
import com.pieta.piscosmetics.server.CosmeticParticleServerHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;

/**
 * Fabric version of the initiation file
 */
public final class PisCosmeticsFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        ModItems.register((id, item) -> Registry.register(BuiltInRegistries.ITEM, id, item));
        ModParticles.register((id, type) -> Registry.register(BuiltInRegistries.PARTICLE_TYPE, id, type));
        FabricNetworking.registerCommon();

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> PisCosmetics.onPlayerJoin(handler.player));

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                CosmeticParticleServerHandler.onPlayerTick(player);
            }
        });

        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new FabricCosmeticDataLoader());
    }
}
