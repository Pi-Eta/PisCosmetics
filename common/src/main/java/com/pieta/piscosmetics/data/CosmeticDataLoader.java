package com.pieta.piscosmetics.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.pieta.piscosmetics.api.CosmeticDefinition;
import com.pieta.piscosmetics.api.KeyItemTypes;
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
    // Swapped whole, never edited in place: in singleplayer the server reloads these while the client draws from them
    private static volatile Map<ResourceLocation, CosmeticDefinition> DEFINITIONS = Map.of();
    /** Item ID -> cosmetic whose render_for names it */
    private static volatile Map<ResourceLocation, ResourceLocation> STAND_INS = Map.of();

    public CosmeticDataLoader() {
        super(GSON, "cosmetic");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, CosmeticDefinition> loaded = new HashMap<>();
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
                // Otherwise a typo would make a key item look craftable from the blueprint base
                if (def != null && !KeyItemTypes.unknown(def.keyItems()).isEmpty()) {
                    System.err.println("PARSE ERROR for " + id + ": unknown key_items " + KeyItemTypes.unknown(def.keyItems())
                            + ", expected any of " + KeyItemTypes.ALL + ". Skipping this cosmetic");
                    def = null;
                }
                if (def != null) {
                    System.out.println("Loaded: " + id + " hide_armor=" + def.hideArmor());
                    loaded.put(id, def);
                }
            } catch (Exception e) {
                System.err.println("EXCEPTION loading " + id + ": " + e.getMessage());
            }
        }
        System.out.println("=== Loaded " + loaded.size() + " cosmetics ===");
        setDefinitions(loaded);
    }

    /** Replaces all definitions, e.g. with the server's */
    public static void setDefinitions(Map<ResourceLocation, CosmeticDefinition> definitions) {
        Map<ResourceLocation, CosmeticDefinition> defs = Map.copyOf(definitions);
        Map<ResourceLocation, ResourceLocation> standIns = buildStandIns(defs);
        DEFINITIONS = defs;
        STAND_INS = standIns;
    }

    /** Sorted so server and clients pick the same cosmetic when two name one item */
    private static Map<ResourceLocation, ResourceLocation> buildStandIns(Map<ResourceLocation, CosmeticDefinition> defs) {
        Map<ResourceLocation, ResourceLocation> standIns = new HashMap<>();
        List<ResourceLocation> ids = new ArrayList<>(defs.keySet());
        ids.sort(Comparator.comparing(ResourceLocation::toString));
        for (ResourceLocation cosmeticId : ids) {
            for (ResourceLocation item : defs.get(cosmeticId).renderFor()) {
                ResourceLocation earlier = standIns.putIfAbsent(item, cosmeticId);
                if (earlier != null) {
                    System.err.println("render_for: " + item + " is named by both " + earlier + " and " + cosmeticId
                            + "; using " + earlier);
                }
            }
        }
        return Map.copyOf(standIns);
    }

    /** Cosmetic whose render_for names this item, or null */
    public static ResourceLocation getStandIn(ResourceLocation itemId) {
        return itemId == null ? null : STAND_INS.get(itemId);
    }

    /** Items named by some render_for, for /piscosmetics_debug */
    public static int standInCount() {
        return STAND_INS.size();
    }

    public static CosmeticDefinition getDefinition(ResourceLocation id) {
        return DEFINITIONS.get(id);
    }

    public static Map<ResourceLocation, CosmeticDefinition> getAllDefinitions() {
        return DEFINITIONS;
    }
}