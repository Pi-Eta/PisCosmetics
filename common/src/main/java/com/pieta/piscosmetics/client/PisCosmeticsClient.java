package com.pieta.piscosmetics.client;

import com.pieta.piscosmetics.ModCreativeTab;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.CreativeModeTab;
import com.pieta.piscosmetics.compat.accessories.AccessoriesCompat;
import com.pieta.piscosmetics.platform.Services;

/**
 * Client only functions
 */
public final class PisCosmeticsClient {

    private PisCosmeticsClient() {}

    /** Called once per tick */
    public static void onClientTick() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) MovementState.clear();
        else MovementState.prune(minecraft.level.getGameTime());
        CosmeticParticleHandler.tick();
        CosmeticDebug.tick();
    }

    /**
     * Called once during client startup, making Accessories use custom renderer if installed
     */
    public static void clientSetup() {
        if (Services.PLATFORM.isModLoaded("accessories")) AccessoriesCompat.initClient();
    }

    /** New definitions from the server; Minecraft won't refill the creative tab by itself */
    public static void onDefinitionsChanged() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || ModCreativeTab.TAB == null) return;
        ModCreativeTab.TAB.buildContents(new CreativeModeTab.ItemDisplayParameters(
                minecraft.player.connection.enabledFeatures(),
                minecraft.player.canUseGameMasterBlocks() && minecraft.options.operatorItemsTab().get(),
                minecraft.level.registryAccess()));
    }
}
