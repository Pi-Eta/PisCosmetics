package com.pieta.piscosmetics.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * /piscosmetics_debug stress [action]: repeats things that might make worn cosmetics stop drawing, then waits in
 * third person so CosmeticDebug's watchdog can check. Stops and names the action when it catches it.
 * Keys are pressed the same way real presses are, so other mods see them too
 */
public final class CosmeticStressTest {

    /** Runs step(minecraft, tick) for burstTicks, then waits settleTicks */
    private record Action(String name, int burstTicks, int settleTicks, BiConsumer<Minecraft, Integer> step) {}

    private static final List<Action> ACTIONS = List.of(
            // Rapid F5
            new Action("camera", 40, 100, (mc, t) -> {
                if (t % 2 == 0) press(mc.options.keyTogglePerspective);
            }),
            new Action("inventory", 60, 100, (mc, t) -> {
                if (t % 20 == 0 && mc.screen == null) press(mc.options.keyInventory);
                if (t % 20 == 10 && mc.screen instanceof AbstractContainerScreen<?>) mc.player.closeContainer();
            }),
            new Action("sneak", 40, 100, (mc, t) -> mc.options.keyShift.setDown(t % 10 < 5)),
            new Action("jump", 40, 100, (mc, t) -> mc.options.keyJump.setDown(true)),
            // Forward then back, so you end up about where you started
            new Action("walk", 40, 100, (mc, t) -> {
                mc.options.keyUp.setDown(t < 20);
                mc.options.keySprint.setDown(t < 20);
                mc.options.keyDown.setDown(t >= 20);
            }),
            new Action("hotbar", 40, 100, (mc, t) -> {
                if (t % 2 == 0) press(mc.options.keyHotbarSlots[(t / 2) % 9]);
            }),
            new Action("pause", 30, 100, (mc, t) -> {
                if (t == 0) mc.pauseGame(false);
                if (t == 20 && mc.screen instanceof PauseScreen) mc.setScreen(null);
            }),
            // F3+A
            new Action("chunks", 1, 100, (mc, t) -> mc.levelRenderer.allChanged()),
            new Action("reload", 1, 300, (mc, t) -> mc.player.connection.sendCommand("reload")),
            // F3+T, slow in a big pack
            new Action("resources", 1, 900, (mc, t) -> mc.reloadResourcePacks())
    );

    private static final int WARM_UP_TICKS = 100;

    private static List<Action> queue;
    private static int index;
    private static int tick;
    private static boolean settling;
    private static int rounds;

    private CosmeticStressTest() {}

    public static List<String> names() {
        return ACTIONS.stream().map(Action::name).toList();
    }

    public static boolean running() {
        return queue != null;
    }

    public static String current() {
        if (queue == null) return "nothing";
        if (!settling && tick < 0) return "the starting wait";
        return queue.get(index).name() + (settling ? " (waiting after it)" : "");
    }

    /** Null runs every action in turn */
    public static void start(String only) {
        Minecraft minecraft = Minecraft.getInstance();
        if (only == null) {
            queue = ACTIONS;
        } else {
            queue = ACTIONS.stream().filter(action -> action.name().equals(only)).toList();
            if (queue.isEmpty()) {
                queue = null;
                chat("Unknown action " + only + ", use one of " + names());
                return;
            }
        }
        index = 0;
        tick = -WARM_UP_TICKS;  // the watchdog sees the starting state first
        settling = false;
        rounds = 0;
        minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        CosmeticDebug.event("stress test started: " + (only == null ? "all actions" : only));
        chat("Stress test running " + (only == null ? names() : only) + ". Stand somewhere flat and safe, "
                + "don't touch anything. /piscosmetics_debug stop to stop");
    }

    public static void stop(String reason) {
        if (queue == null) return;
        Minecraft minecraft = Minecraft.getInstance();
        releaseInputs(minecraft);
        queue = null;
        CosmeticDebug.event("stress test stopped: " + reason);
        chat("Stress test stopped after " + rounds + " full rounds: " + reason);
    }

    public static void tick(Minecraft minecraft) {
        if (queue == null || minecraft.player == null) return;
        Action action = queue.get(index);
        if (!settling) {
            if (tick < 0) {
                tick++;
                return;
            }
            if (tick == 0) CosmeticDebug.event("stress: " + action.name());
            action.step().accept(minecraft, tick);
            if (++tick >= action.burstTicks()) {
                settling = true;
                tick = 0;
                releaseInputs(minecraft);
            }
        } else if (++tick >= action.settleTicks()) {
            settling = false;
            tick = 0;
            index = (index + 1) % queue.size();
            if (index == 0 && ++rounds % 5 == 0) chat("Stress test: " + rounds + " rounds, nothing yet");
        }
    }

    /** Back to standing still in third person with no menu open */
    private static void releaseInputs(Minecraft minecraft) {
        minecraft.options.keyShift.setDown(false);
        minecraft.options.keyJump.setDown(false);
        minecraft.options.keyUp.setDown(false);
        minecraft.options.keyDown.setDown(false);
        minecraft.options.keySprint.setDown(false);
        if (minecraft.screen instanceof AbstractContainerScreen<?> && minecraft.player != null) {
            minecraft.player.closeContainer();
        } else if (minecraft.screen instanceof PauseScreen) {
            minecraft.setScreen(null);
        }
        if (minecraft.options.getCameraType() != CameraType.THIRD_PERSON_BACK) {
            minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        }
    }

    // Same path as a real key press
    private static void press(KeyMapping key) {
        KeyMapping.click(InputConstants.getKey(key.saveString()));
    }

    private static void chat(String text) {
        Minecraft.getInstance().gui.getChat().addMessage(Component.literal(text).withStyle(ChatFormatting.GRAY));
    }
}
