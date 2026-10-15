package com.pieta.piscosmetics.client;

import com.pieta.piscosmetics.ModItems;
import com.pieta.piscosmetics.client.renderer.CosmeticAccessoryRenderer;
import io.wispforest.accessories.api.client.AccessoriesRendererRegistry;
import net.minecraft.world.item.Item;

/**
 * Client only functions
 */
public final class PisCosmeticsClient {

    private PisCosmeticsClient() {}

    /** Called once per tick */
    public static void onClientTick() {
        MovementCalculator.tick();
        MovementInertia.tick();
        CosmeticParticleHandler.tick();
    }

    /**
     * Called once during client startup, making Accessories use custom renderer
     */
    public static void clientSetup() {
        for (Item item : ModItems.ALL) {
            AccessoriesRendererRegistry.registerRenderer(item, CosmeticAccessoryRenderer::new);
        }
    }
}
