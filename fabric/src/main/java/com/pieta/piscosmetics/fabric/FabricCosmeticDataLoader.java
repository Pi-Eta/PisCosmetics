package com.pieta.piscosmetics.fabric;

import com.pieta.piscosmetics.PisCosmetics;
import com.pieta.piscosmetics.data.CosmeticDataLoader;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;

/**
 * Loads cosmetics from the datapack
 */
public final class FabricCosmeticDataLoader extends CosmeticDataLoader implements IdentifiableResourceReloadListener {

    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(PisCosmetics.MODID, "cosmetics");

    @Override
    public ResourceLocation getFabricId() {
        return ID;
    }
}
