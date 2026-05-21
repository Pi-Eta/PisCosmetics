package com.pieta.piscosmetics.client;

import net.minecraft.world.entity.player.Player;

public class AnimationResolver {

    public record Result(
            String baseAnimation,
            String overlayAnimation
    ) {}

    public static Result resolve(MovementState.State state, Player player) {

        // -------------------------
        // GLIDE (highest priority)
        // -------------------------
        if (state.gliding || player.isFallFlying()) {
            return new Result("glide", "");
        }

        // -------------------------
        // BASE MOVEMENT
        // -------------------------
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

        // -------------------------
        // AIR OVERLAY
        // -------------------------
        String overlay = "";

        if (!state.grounded && !state.gliding) {
            overlay = player.getDeltaMovement().y > 0 ? "jump" : "fall";
        }

        return new Result(base, overlay);
    }
}