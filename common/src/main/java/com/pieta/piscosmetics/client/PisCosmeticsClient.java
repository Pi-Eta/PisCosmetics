package com.pieta.piscosmetics.client;

import com.pieta.piscosmetics.compat.accessories.AccessoriesCompat;
import com.pieta.piscosmetics.platform.Services;

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
     * Called once during client startup, making Accessories use custom renderer if installed
     */
    public static void clientSetup() {
        if (Services.PLATFORM.isModLoaded("accessories")) AccessoriesCompat.initClient();
    }
}
