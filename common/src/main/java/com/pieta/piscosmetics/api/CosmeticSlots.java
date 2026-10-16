package com.pieta.piscosmetics.api;

import com.pieta.piscosmetics.item.CosmeticHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * The one place that answers "which cosmetics is this entity wearing?".
 * <p>
 * Each {@link Source} is a place items can be worn. The vanilla armor slots are always a source.
 * Optional mods add their own: Accessories adds its slots in AccessoriesCompat, only when it is installed,
 * so nothing outside compat/accessories ever touches an Accessories class.
 */
public final class CosmeticSlots {

    /** A place items can be worn. It hands every worn stack to {@code out}; empty stacks are fine. */
    public interface Source {
        void collect(LivingEntity entity, Consumer<ItemStack> out);
    }

    private static final List<Source> SOURCES = new ArrayList<>(List.of(
            (entity, out) -> {
                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) out.accept(entity.getItemBySlot(slot));
                }
            }));

    private CosmeticSlots() {}

    /** Adds a place items can be worn. Call once, during startup. */
    public static void addSource(Source source) {
        SOURCES.add(source);
    }

    /** Every worn stack, from every source, including stacks that aren't cosmetics. */
    public static List<ItemStack> getWornStacks(LivingEntity entity) {
        List<ItemStack> stacks = new ArrayList<>();
        for (Source source : SOURCES) {
            source.collect(entity, stack -> {
                if (!stack.isEmpty()) stacks.add(stack);
            });
        }
        return stacks;
    }

    /** The definition of every worn cosmetic that has one. */
    public static List<CosmeticDefinition> getWorn(LivingEntity entity) {
        List<CosmeticDefinition> worn = new ArrayList<>();
        for (ItemStack stack : getWornStacks(entity)) {
            CosmeticDefinition def = CosmeticHelper.getDefinition(stack);
            if (def != null) worn.add(def);
        }
        return worn;
    }
}
