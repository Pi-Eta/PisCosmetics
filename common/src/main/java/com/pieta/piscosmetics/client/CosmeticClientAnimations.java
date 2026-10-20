package com.pieta.piscosmetics.client;

import com.pieta.piscosmetics.item.CosmeticArmorItem;
import com.pieta.piscosmetics.item.CosmeticItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.RawAnimation;

import java.util.HashMap;
import java.util.Map;

/**
 * One movement controller per cosmetic. Follows whoever is wearing it (CosmeticWearer), idle when not worn
 */
public final class CosmeticClientAnimations {

    private static final Map<String, RawAnimation> LOOPS = new HashMap<>();

    private CosmeticClientAnimations() {}

    public static void registerControllers(CosmeticItem item, AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(createController(item));
    }

    public static void registerControllers(CosmeticArmorItem item, AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(createController(item));
    }

    private static <T extends Item & GeoItem> AnimationController<T> createController(T item) {
        return new AnimationController<>(item, "movement_controller", 10, CosmeticClientAnimations::play);
    }

    private static <T extends Item & GeoItem> PlayState play(AnimationState<T> state) {
        LivingEntity wearer = CosmeticWearer.get();
        String name = wearer == null
                ? "idle"
                : AnimationResolver.resolve(MovementState.get(wearer), wearer).baseAnimation();
        return state.setAndContinue(LOOPS.computeIfAbsent(name, n -> RawAnimation.begin().thenLoop(n)));
    }
}
