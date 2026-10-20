package com.pieta.piscosmetics.client;

import net.minecraft.world.entity.LivingEntity;

/**
 * Entity whose cosmetic is being drawn right now, set by CosmeticRenderer. Lets animations and
 * GeckoLib instance IDs be per wearer. Render thread only; null outside a worn render (GUI, hand, ground)
 */
public final class CosmeticWearer {

    private static LivingEntity current;

    private CosmeticWearer() {}

    public static LivingEntity get() {
        return current;
    }

    public static void set(LivingEntity entity) {
        current = entity;
    }
}
