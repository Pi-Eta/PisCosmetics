package com.pieta.piscosmetics.neoforge;

import com.pieta.piscosmetics.ModCreativeTab;
import com.pieta.piscosmetics.ModItems;
import com.pieta.piscosmetics.ModParticles;
import com.pieta.piscosmetics.ModRecipes;
import com.pieta.piscosmetics.PisCosmetics;
import com.pieta.piscosmetics.data.CosmeticDataLoader;
import com.pieta.piscosmetics.server.CosmeticParticleServerHandler;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

/**
 * NeoForge entry point
 */
@Mod(PisCosmetics.MODID)
public final class PisCosmeticsNeoForge {

    public PisCosmeticsNeoForge(IEventBus modBus) {
        // Registries only  during RegisterEvent on NeoForge
        modBus.addListener(RegisterEvent.class, event -> {
            event.register(Registries.ITEM, helper -> ModItems.register(helper::register));
            event.register(Registries.PARTICLE_TYPE, helper -> ModParticles.register(helper::register));
            event.register(Registries.RECIPE_SERIALIZER, helper -> ModRecipes.register(helper::register));
            event.register(Registries.CREATIVE_MODE_TAB, helper -> ModCreativeTab.register(CreativeModeTab::builder, helper::register));
        });
        modBus.addListener(RegisterPayloadHandlersEvent.class, NeoForgeNetworking::register);
        // No items exist yet while this constructor runs, so optional-mod setup waits for common setup
        modBus.addListener(FMLCommonSetupEvent.class, event -> event.enqueueWork(PisCosmetics::commonSetup));

        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedInEvent.class, event -> {
            if (event.getEntity() instanceof ServerPlayer player) PisCosmetics.sendDefinitions(player);
        });
        // Resend after /reload
        NeoForge.EVENT_BUS.addListener(OnDatapackSyncEvent.class, event -> {
            if (event.getPlayer() == null) event.getRelevantPlayers().forEach(PisCosmetics::sendDefinitions);
        });
        NeoForge.EVENT_BUS.addListener(PlayerTickEvent.Post.class, event -> {
            if (event.getEntity() instanceof ServerPlayer player) CosmeticParticleServerHandler.onPlayerTick(player);
        });
        NeoForge.EVENT_BUS.addListener(AddReloadListenerEvent.class,
                event -> event.addListener(new CosmeticDataLoader()));
    }
}
