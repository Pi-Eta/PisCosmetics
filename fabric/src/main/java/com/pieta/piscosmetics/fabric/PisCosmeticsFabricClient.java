package com.pieta.piscosmetics.fabric;

import com.pieta.piscosmetics.ModParticles;
import com.pieta.piscosmetics.client.CosmeticParticle;
import com.pieta.piscosmetics.client.PisCosmeticsClient;
import com.pieta.piscosmetics.fabric.client.FabricClientNetworking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;

/**
 * Fabric's "client" entry point
 */
public final class PisCosmeticsFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        FabricClientNetworking.register();
        ParticleFactoryRegistry.getInstance().register(ModParticles.COSMETIC_PARTICLE, new CosmeticParticle.Provider());
        ClientTickEvents.END_CLIENT_TICK.register(client -> PisCosmeticsClient.onClientTick());
        PisCosmeticsClient.clientSetup();
    }
}
