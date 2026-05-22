package com.pieta.piscosmetics.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class MovementInertia {

    private static float smoothedStrafe = 0f;
    private static float smoothedForward = 0f;

    // lower = more floaty, higher = more responsive
    private static final float SMOOTHING = 0.25f;

    public static void tick() {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        float targetStrafe = player.xxa;
        float targetForward = player.zza;

        smoothedStrafe += (targetStrafe - smoothedStrafe) * SMOOTHING;
        smoothedForward += (targetForward - smoothedForward) * SMOOTHING;
    }

    public static float strafe() {
        return smoothedStrafe;
    }

    public static float forward() {
        return smoothedForward;
    }
}