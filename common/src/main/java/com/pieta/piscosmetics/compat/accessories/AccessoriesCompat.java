package com.pieta.piscosmetics.compat.accessories;

import com.pieta.piscosmetics.ModItems;
import com.pieta.piscosmetics.api.CosmeticDefinition;
import com.pieta.piscosmetics.api.CosmeticSlotNames;
import com.pieta.piscosmetics.api.CosmeticSlots;
import com.pieta.piscosmetics.item.CosmeticHelper;
import com.pieta.piscosmetics.client.CosmeticDebug;
import io.wispforest.accessories.Accessories;
import io.wispforest.accessories.api.AccessoriesAPI;
import io.wispforest.accessories.api.AccessoriesContainer;
import io.wispforest.accessories.api.client.AccessoryRenderer;
import net.minecraft.world.entity.LivingEntity;
import io.wispforest.accessories.data.EntitySlotLoader;
import io.wispforest.accessories.utils.ValidatingForwardingMap;
import io.wispforest.accessories.impl.AccessoriesHolderImpl;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.Map;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.Accessory;
import io.wispforest.accessories.api.client.AccessoriesRendererRegistry;
import io.wispforest.accessories.api.slot.SlotReference;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Everything that needs the Accessories mod.
 * <p>
 * Only ever called behind {@code Services.PLATFORM.isModLoaded("accessories")}. Java loads a class the
 * first time code needs it, so as long as nothing else mentions an Accessories type, the mod runs fine
 * without Accessories installed. Keep every {@code io.wispforest} usage inside this package.
 */
public final class AccessoriesCompat {

    private AccessoriesCompat() {}

    /** Both sides, after items are registered. */
    public static void init() {
        // Cosmetics in Accessories slots count as worn (hide_armor, particles)
        CosmeticSlots.addSource((entity, out) -> {
            AccessoriesCapability capability = AccessoriesCapability.get(entity);
            if (capability != null) capability.getAllEquipped().forEach(reference -> out.accept(reference.stack()));
        });

        // The universal item is in every slot tag (data/accessories/tags/item), so every slot would accept it.
        // canEquip narrows that to the one slot its JSON names. Runs on both sides; the client knows
        // each cosmetic's slot because CosmeticSyncPacket sends the definitions.
        AccessoriesAPI.registerAccessory(ModItems.COSMETIC, new Accessory() {
            @Override
            public boolean canEquip(ItemStack stack, SlotReference reference) {
                CosmeticDefinition def = CosmeticHelper.getDefinition(stack);
                return def != null && CosmeticSlotNames.normalize(def.slot())
                        .equals(CosmeticSlotNames.normalize(reference.slotName()));
            }
        });
    }

    /**
     * Client only, for CosmeticDebug: every filled Accessories slot as the render layer sees it.
     * Returns the number of slot types, or -1 without a capability
     */
    public static int forEachWorn(LivingEntity entity, CosmeticDebug.WornVisitor visitor) {
        AccessoriesCapability capability = AccessoriesCapability.get(entity);
        if (capability == null) return -1;
        Map<String, AccessoriesContainer> all = containersOf(entity, capability.getContainers());
        all.forEach((name, container) -> {
            for (int i = 0; i < container.getAccessories().getContainerSize(); i++) {
                ItemStack stack = container.getAccessories().getItem(i);
                ItemStack cosmetic = container.getCosmeticAccessories().getItem(i);
                // Same choice as AccessoriesRenderLayer
                if (!cosmetic.isEmpty() && Accessories.config().clientOptions.showCosmeticAccessories()) stack = cosmetic;
                if (stack.isEmpty()) continue;

                AccessoryRenderer renderer = AccessoriesRendererRegistry.getRenderer(stack);
                boolean drawn = !renderer.isEmpty() && renderer.shouldRender(container.shouldRender(i));
                String info = " | renderer=" + renderer.getClass().getSimpleName()
                        + (renderer.isEmpty() ? " (empty)" : "") + (drawn ? "" : " HIDDEN");
                visitor.visit(name + "#" + i, stack, info, drawn);
            }
        });
        return all.size();
    }

    /** Iterated normally, or looked up one by one when the map can't be iterated (see AccessoriesWrappingCollectionMixin) */
    private static Map<String, AccessoriesContainer> containersOf(LivingEntity entity, Map<String, AccessoriesContainer> containers) {
        Map<String, AccessoriesContainer> all = new LinkedHashMap<>();
        containers.forEach(all::put);
        if (all.isEmpty() && containers.size() > 0) {
            for (String name : EntitySlotLoader.getEntitySlots(entity).keySet()) {
                AccessoriesContainer container = containers.get(name);
                if (container != null) all.put(name, container);
            }
        }
        return all;
    }

    /**
     * Client only, for CosmeticDebug. "filtered" means Accessories serves this entity's containers through its
     * filtered map, which can't be iterated in beta.53/54; "BROKEN" means our fix for that isn't active either
     */
    public static String containerState(LivingEntity entity) {
        AccessoriesCapability capability = AccessoriesCapability.get(entity);
        if (capability == null) return "no capability";
        Map<String, AccessoriesContainer> containers = capability.getContainers();
        if (!(containers instanceof ValidatingForwardingMap<?, ?>)) return "normal";
        boolean iterable = containers.isEmpty() || containers.entrySet().iterator().hasNext();
        return "filtered (" + containers.size() + " of " + EntitySlotLoader.getEntitySlots(entity).size() + " slot types)"
                + (iterable ? "" : ", BROKEN: can't be iterated, nothing will draw");
    }

    /**
     * Client only, /piscosmetics_debug break_accessories: puts this entity into the filtered state on purpose by
     * naming one slot type it has no container for, the same thing setValidTypes does by itself. Cleared by
     * Accessories' next full sync (/reload, rejoin)
     */
    public static boolean forceFiltered(LivingEntity entity) {
        AccessoriesCapability capability = AccessoriesCapability.get(entity);
        if (capability == null || !(capability.getHolder() instanceof AccessoriesHolderImpl holder)) return false;
        Set<String> slotTypes = new HashSet<>(EntitySlotLoader.getEntitySlots(entity).keySet());
        slotTypes.add("piscosmetics_missing_slot");
        holder.setValidTypes(slotTypes);
        return true;
    }

    /** Client only, after items are registered: Accessories draws every cosmetic with our renderer. */
    public static void initClient() {
        for (Item item : ModItems.ALL) {
            AccessoriesRendererRegistry.registerRenderer(item, CosmeticAccessoryRenderer::new);
        }
    }
}
