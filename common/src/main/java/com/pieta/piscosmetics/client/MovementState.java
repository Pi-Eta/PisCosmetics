package com.pieta.piscosmetics.client;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MovementState {

    public static class State {

        // Horizontal movement direction (-1 to 1)
        public final float strafe;   // left/right
        public final float forward;  // forward/back

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

    private static final Map<UUID, State> STATES = new ConcurrentHashMap<>();

    public static void set(UUID uuid, State state) {
        STATES.put(uuid, state);
    }

    public static State get(UUID uuid) {
        return STATES.getOrDefault(uuid, State.idle());
    }
}