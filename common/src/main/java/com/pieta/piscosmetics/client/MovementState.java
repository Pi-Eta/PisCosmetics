package com.pieta.piscosmetics.client;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * How each wearer is moving, worked out from their position each tick so it works for other players
 * and armor stands too (key presses only exist for the local player). Client thread only
 */
public class MovementState {

    public static class State {

        // Horizontal movement direction (-1 to 1)
        public final float strafe;   // positive = left
        public final float forward;  // positive = forward

        // Air state flags
        public final boolean gliding;
        public final boolean grounded;

        public State(float strafe, float forward, boolean gliding, boolean grounded) {
            this.strafe = strafe;
            this.forward = forward;
            this.gliding = gliding;
            this.grounded = grounded;
        }

        public static State idle() {
            return new State(0f, 0f, false, true);
        }
    }

    private static final class Tracker {
        int lastTick = Integer.MIN_VALUE;
        long lastSeen;
        float strafe;
        float forward;
    }

    // Blocks per tick that count as full speed (walking is about 0.22)
    private static final float FULL_SPEED = 0.2f;
    // Below this many blocks per tick counts as standing still, hides interpolation jitter
    private static final float DEADZONE = 0.01f;
    // lower = more floaty, higher = more responsive
    private static final float SMOOTHING = 0.25f;
    // Ticks a wearer can go unseen before their state is dropped
    private static final int FORGET_AFTER = 200;

    private static final Map<UUID, Tracker> TRACKERS = new HashMap<>();

    public static State get(LivingEntity entity) {
        if (entity == null) return State.idle();
        Tracker tracker = TRACKERS.computeIfAbsent(entity.getUUID(), id -> new Tracker());

        // Once per tick, however often it's drawn (shadow pass, several cosmetics)
        if (tracker.lastTick != entity.tickCount) {
            tracker.lastTick = entity.tickCount;
            float targetStrafe = 0f;
            float targetForward = 0f;

            // Riding moves the wearer without walking
            if (!entity.isPassenger()) {
                double dx = entity.getX() - entity.xo;
                double dz = entity.getZ() - entity.zo;
                if (dx * dx + dz * dz > DEADZONE * DEADZONE) {
                    // Into the wearer's view direction, the same frame movement keys use
                    float yaw = entity.getYRot() * Mth.DEG_TO_RAD;
                    double sin = Mth.sin(yaw);
                    double cos = Mth.cos(yaw);
                    targetForward = Mth.clamp((float) (dz * cos - dx * sin) / FULL_SPEED, -1f, 1f);
                    targetStrafe = Mth.clamp((float) (dx * cos + dz * sin) / FULL_SPEED, -1f, 1f);
                }
            }

            tracker.strafe += (targetStrafe - tracker.strafe) * SMOOTHING;
            tracker.forward += (targetForward - tracker.forward) * SMOOTHING;
        }

        tracker.lastSeen = entity.level().getGameTime();
        return new State(tracker.strafe, tracker.forward, entity.isFallFlying(), entity.onGround());
    }

    /** Drops wearers not drawn for a while. Called every client tick */
    public static void prune(long gameTime) {
        if (gameTime % FORGET_AFTER != 0) return;
        TRACKERS.values().removeIf(tracker -> gameTime - tracker.lastSeen > FORGET_AFTER);
    }

    /** Leaving a world */
    public static void clear() {
        TRACKERS.clear();
    }
}
