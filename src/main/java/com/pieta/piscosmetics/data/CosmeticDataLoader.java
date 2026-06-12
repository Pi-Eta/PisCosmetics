package com.pieta.piscosmetics.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.pieta.piscosmetics.api.CosmeticDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;

import java.util.HashMap;
import java.util.Map;

public class CosmeticDataLoader extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().create();
    public static final Map<ResourceLocation, CosmeticDefinition> DEFINITIONS = new HashMap<>();

    public CosmeticDataLoader() {
        super(GSON, "cosmetic");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager resourceManager, ProfilerFiller profiler) {
        DEFINITIONS.clear();
        System.out.println("=== Loading cosmetics ===");
        for (var entry : objects.entrySet()) {
            ResourceLocation id = entry.getKey();
            System.out.println("Loading: " + id);
            try {
                CosmeticDefinition def = CosmeticDefinition.CODEC.parse(JsonOps.INSTANCE, entry.getValue())
                        .resultOrPartial(error -> {
                            System.err.println("PARSE ERROR for " + id + ": " + error);
                        })
                        .orElse(null);
                if (def != null) {
                    System.out.println("Loaded: " + id + " hide_armor=" + def.hideArmor());
                    DEFINITIONS.put(id, def);
                }
            } catch (Exception e) {
                System.err.println("EXCEPTION loading " + id + ": " + e.getMessage());
            }
        }
        System.out.println("=== Loaded " + DEFINITIONS.size() + " cosmetics ===");
    }

    public static CosmeticDefinition getDefinition(ResourceLocation id) {
        return DEFINITIONS.get(id);
    }

    public static Map<ResourceLocation, CosmeticDefinition> getAllDefinitions() {
        return Map.copyOf(DEFINITIONS);
    }
}