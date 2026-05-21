package com.pieta.piscosmetics;

import com.pieta.piscosmetics.client.renderer.CosmeticAccessoryRenderer;
import com.pieta.piscosmetics.data.CosmeticDataLoader;
import com.pieta.piscosmetics.network.CosmeticSyncPacket;
import io.wispforest.accessories.api.client.AccessoriesRendererRegistry;
import net.neoforged.bus.api.IEventBus;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
@Mod(PisCosmetics.MODID)
public class PisCosmetics {
    public static final String MODID = "piscosmetics";

    public PisCosmetics(IEventBus modEventBus) {
        ModItems.ITEMS.register(modEventBus);
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(CosmeticSyncPacket::register);
        NeoForge.EVENT_BUS.addListener(this::onPlayerLogin);
        NeoForge.EVENT_BUS.addListener(this::onAddReloadListener);
    }

    private void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer,
                    new CosmeticSyncPacket(CosmeticDataLoader.getAllDefinitions()));
        }
    }

    private void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(new CosmeticDataLoader());
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            AccessoriesRendererRegistry.registerRenderer(
                    ModItems.COSMETIC_ANKLET.get(),
                    CosmeticAccessoryRenderer::new
            );
            AccessoriesRendererRegistry.registerRenderer(
                    ModItems.COSMETIC_BACK.get(),
                    CosmeticAccessoryRenderer::new
            );
            AccessoriesRendererRegistry.registerRenderer(
                    ModItems.COSMETIC_BELT.get(),
                    CosmeticAccessoryRenderer::new
            );
            AccessoriesRendererRegistry.registerRenderer(
                    ModItems.COSMETIC_CAPE.get(),
                    CosmeticAccessoryRenderer::new
            );
            AccessoriesRendererRegistry.registerRenderer(
                    ModItems.COSMETIC_CHARM.get(),
                    CosmeticAccessoryRenderer::new
            );
            AccessoriesRendererRegistry.registerRenderer(
                    ModItems.COSMETIC_FACE.get(),
                    CosmeticAccessoryRenderer::new
            );
            AccessoriesRendererRegistry.registerRenderer(
                    ModItems.COSMETIC_HAND.get(),
                    CosmeticAccessoryRenderer::new
            );
            AccessoriesRendererRegistry.registerRenderer(
                    ModItems.COSMETIC_HAT.get(),
                    CosmeticAccessoryRenderer::new
            );
            AccessoriesRendererRegistry.registerRenderer(
                    ModItems.COSMETIC_NECKLACE.get(),
                    CosmeticAccessoryRenderer::new
            );
            AccessoriesRendererRegistry.registerRenderer(
                    ModItems.COSMETIC_RING.get(),
                    CosmeticAccessoryRenderer::new
            );
            AccessoriesRendererRegistry.registerRenderer(
                    ModItems.COSMETIC_SHOES.get(),
                    CosmeticAccessoryRenderer::new
            );
            AccessoriesRendererRegistry.registerRenderer(
                    ModItems.COSMETIC_WRIST.get(),
                    CosmeticAccessoryRenderer::new
            );
            AccessoriesRendererRegistry.registerRenderer(
                    ModItems.COSMETIC_DYNAMAX_BAND.get(),
                    CosmeticAccessoryRenderer::new
            );
            AccessoriesRendererRegistry.registerRenderer(
                    ModItems.COSMETIC_MEGA_BRACELET.get(),
                    CosmeticAccessoryRenderer::new
            );
            AccessoriesRendererRegistry.registerRenderer(
                    ModItems.COSMETIC_TERA_ORB.get(),
                    CosmeticAccessoryRenderer::new
            );
            AccessoriesRendererRegistry.registerRenderer(
                    ModItems.COSMETIC_Z_RING.get(),
                    CosmeticAccessoryRenderer::new
            );
        });
    }
}