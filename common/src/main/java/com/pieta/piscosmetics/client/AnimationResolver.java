package com.pieta.piscosmetics.client;

import net.minecraft.world.entity.LivingEntity;

public class AnimationResolver {

    public record Result(
            String baseAnimation,
            String overlayAnimation
    ) {}

    public static Result resolve(MovementState.State state, LivingEntity entity) {

        // GLIDE
        if (state.gliding) {
            return new Result("glide", "");
        }

        // BASE MOVEMENT
        String base;

        float f = state.forward;
        float s = state.strafe;

        if (Math.abs(f) < 0.01f && Math.abs(s) < 0.01f) {
            base = "idle";
        } else if (Math.abs(f) >= Math.abs(s)) {
            base = f > 0 ? "walk_forward" : "walk_backward";
        } else {
            base = s > 0 ? "walk_left" : "walk_right";
        }

        // AIR OVERLAY
        String overlay = "";

        if (!state.grounded) {
            // Position change, since other players' velocity isn't known client side
            overlay = entity.getY() - entity.yo > 0 ? "jump" : "fall";
        }

        return new Result(base, overlay);
    }
}
