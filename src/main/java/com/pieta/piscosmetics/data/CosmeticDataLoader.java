package com.pieta.piscosmetics.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import com.pieta.piscosmetics.api.CosmeticDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

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
            System.out.println("Loading: " + id + " -> " + entry.getValue());
            try {
                CosmeticDefinition def = CosmeticDefinition.CODEC.parse(JsonOps.INSTANCE, entry.getValue())
                        .resultOrPartial(error -> {
                            System.err.println("PARSE ERROR for " + id + ": " + error);
                        })
                        .orElse(null);
                if (def != null) {
                    System.out.println("Loaded: " + id + " particles=" + def.particles());
                    DEFINITIONS.put(id, def);
                }
            } catch (Exception e) {
            }
        }
    }

    public static CosmeticDefinition getDefinition(ResourceLocation id) {
        return DEFINITIONS.get(id);
    }

    public static Map<ResourceLocation, CosmeticDefinition> getAllDefinitions() {
        return Map.copyOf(DEFINITIONS);
    }
}