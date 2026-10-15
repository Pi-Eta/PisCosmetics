package com.pieta.piscosmetics.neoforge;

import com.pieta.piscosmetics.ModParticles;
import com.pieta.piscosmetics.PisCosmetics;
import com.pieta.piscosmetics.client.CosmeticParticle;
import com.pieta.piscosmetics.client.PisCosmeticsClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * NeoForge's client-only entry point
 */
@Mod(value = PisCosmetics.MODID, dist = Dist.CLIENT)
public final class PisCosmeticsNeoForgeClient {

    public PisCosmeticsNeoForgeClient(IEventBus modBus) {
        modBus.addListener(RegisterParticleProvidersEvent.class,
                event -> event.registerSpecial(ModParticles.COSMETIC_PARTICLE, new CosmeticParticle.Provider()));
        // FMLClientSetupEvent runs for many mods at once on worker threads, so the work is queued onto the main thread
        modBus.addListener(FMLClientSetupEvent.class, event -> event.enqueueWork(PisCosmeticsClient::clientSetup));

        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, event -> PisCosmeticsClient.onClientTick());
    }
}
