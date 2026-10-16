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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CosmeticDataLoader extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().create();
    public static final Map<ResourceLocation, CosmeticDefinition> DEFINITIONS = new HashMap<>();
    /** Item ID -> the cosmetic whose render_for names it (the default look for that item when worn). */
    private static final Map<ResourceLocation, ResourceLocation> STAND_INS = new HashMap<>();

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
        rebuildStandIns();
    }

    /** Replaces every definition, e.g. with the ones the server sent. */
    public static void setDefinitions(Map<ResourceLocation, CosmeticDefinition> definitions) {
        DEFINITIONS.clear();
        DEFINITIONS.putAll(definitions);
        rebuildStandIns();
    }

    /** Sorted by cosmetic ID, so server and clients pick the same winner when two cosmetics name one item. */
    private static void rebuildStandIns() {
        STAND_INS.clear();
        List<ResourceLocation> ids = new ArrayList<>(DEFINITIONS.keySet());
        ids.sort(Comparator.comparing(ResourceLocation::toString));
        for (ResourceLocation cosmeticId : ids) {
            for (ResourceLocation item : DEFINITIONS.get(cosmeticId).renderFor()) {
                ResourceLocation earlier = STAND_INS.putIfAbsent(item, cosmeticId);
                if (earlier != null) {
                    System.err.println("render_for: " + item + " is named by both " + earlier + " and " + cosmeticId
                            + "; using " + earlier);
                }
            }
        }
    }

    /** The cosmetic whose render_for names this item, or null. */
    public static ResourceLocation getStandIn(ResourceLocation itemId) {
        return itemId == null ? null : STAND_INS.get(itemId);
    }

    public static CosmeticDefinition getDefinition(ResourceLocation id) {
        return DEFINITIONS.get(id);
    }

    public static Map<ResourceLocation, CosmeticDefinition> getAllDefinitions() {
        return Map.copyOf(DEFINITIONS);
    }
}