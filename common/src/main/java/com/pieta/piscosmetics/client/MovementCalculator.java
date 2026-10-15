package com.pieta.piscosmetics.client;

import net.minecraft.client.Minecraft;

public class MovementCalculator {

    public static void tick() {

        var player = Minecraft.getInstance().player;
        if (player == null) return;

        float strafe = MovementInertia.strafe();
        float forward = MovementInertia.forward();

        boolean gliding = player.isFallFlying();
        boolean cosmeticGlide = false;
        boolean grounded = player.onGround();

        MovementState.set(player.getUUID(),
                new MovementState.State(
                        strafe,
                        forward,
                        gliding,
                        grounded
                )
        );
    }
}