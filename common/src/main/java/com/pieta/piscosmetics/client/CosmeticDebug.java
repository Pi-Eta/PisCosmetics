package com.pieta.piscosmetics.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.logging.LogUtils;
import com.pieta.piscosmetics.api.CosmeticSlotNames;
import com.pieta.piscosmetics.compat.accessories.AccessoriesCompat;
import com.pieta.piscosmetics.data.CosmeticDataLoader;
import com.pieta.piscosmetics.item.CosmeticArmorItem;
import com.pieta.piscosmetics.item.CosmeticHelper;
import com.pieta.piscosmetics.platform.Services;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;
import org.slf4j.Logger;

import java.time.LocalTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Finding why worn cosmetics sometimes stop drawing. Client only, render thread only.
 * <ul>
 *   <li>/piscosmetics_debug: what you and the entity you look at wear, and when each cosmetic was last drawn</li>
 *   <li>A watchdog that notices when your cosmetics stop drawing in third person and logs the same report,
 *       plus what happened just before</li>
 *   <li>/piscosmetics_debug stress [action]: CosmeticStressTest, repeats possible triggers until the watchdog fires</li>
 * </ul>
 */
public final class CosmeticDebug {

    private static final Logger LOGGER = LogUtils.getLogger();

    /** One worn item: where it is, the stack, renderer info, and whether it should be drawn */
    public interface WornVisitor {
        void visit(String where, ItemStack stack, String info, boolean drawn);
    }

    private record Draw(long at, boolean finitePose, String boneIssue) {}

    // "<entity id>/<cosmetic id>" -> last draw
    private static final Map<String, Draw> LAST_DRAWN = new HashMap<>();
    private static final float[] MATRIX = new float[16];

    // When the local player's model was last drawn at all (armor layer)
    private static long playerDrawnAt;

    // What happened recently, newest last
    private static final ArrayDeque<String> EVENTS = new ArrayDeque<>();
    private static final int MAX_EVENTS = 100;
    private static String lastCamera;
    private static String lastScreen;
    private static String lastPose;
    private static String lastWorn;
    private static String lastContainerState;
    private static int lastPlayerId = -1;
    private static Level lastLevel;
    private static boolean lastRiding;
    private static boolean lastGliding;

    // Watchdog
    private static final long MISSING_AFTER_MS = 2000;
    private static int badChecks;
    private static boolean incident;

    private CosmeticDebug() {}

    // ---- Commands ----

    public static <S> LiteralArgumentBuilder<S> command() {
        return LiteralArgumentBuilder.<S>literal("piscosmetics_debug")
                .executes(context -> later(() -> dump("Report requested", true)))
                .then(LiteralArgumentBuilder.<S>literal("stress")
                        .executes(context -> later(() -> CosmeticStressTest.start(null)))
                        .then(RequiredArgumentBuilder.<S, String>argument("action", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(CosmeticStressTest.names(), builder))
                                .executes(context -> {
                                    String action = StringArgumentType.getString(context, "action");
                                    return later(() -> CosmeticStressTest.start(action));
                                })))
                .then(LiteralArgumentBuilder.<S>literal("break_accessories")
                        .executes(context -> later(CosmeticDebug::breakAccessories)))
                .then(LiteralArgumentBuilder.<S>literal("stop")
                        .executes(context -> later(() -> CosmeticStressTest.stop("stopped by command"))));
    }

    /** Demonstrates the Accessories bug that AccessoriesWrappingCollectionMixin works around */
    private static void breakAccessories() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;
        if (!Services.PLATFORM.isModLoaded("accessories") || !AccessoriesCompat.forceFiltered(minecraft.player)) {
            chat("Accessories isn't installed, or you have no Accessories data");
            return;
        }
        event("break_accessories: forced the filtered container state");
        chat("Your Accessories containers are now: " + containerState(minecraft.player));
        chat("Without the fix your worn Accessories items stop drawing here, while the Accessories screen still "
                + "shows them. /reload or rejoining brings them back");
    }

    private static int later(Runnable action) {
        Minecraft.getInstance().execute(action);
        return 1;
    }

    // ---- Called from rendering ----

    /** CosmeticArmorRenderMixin, for every humanoid drawn */
    public static void entityDrawn(LivingEntity entity) {
        if (entity == Minecraft.getInstance().player) playerDrawnAt = Util.getMillis();
    }

    /** CosmeticRenderer, right before drawing a cosmetic */
    public static void drawing(LivingEntity entity, ItemStack stack, PoseStack poseStack) {
        String key = key(entity, CosmeticHelper.getCosmeticId(stack));
        Draw previous = LAST_DRAWN.get(key);
        LAST_DRAWN.put(key, new Draw(Util.getMillis(), isFinite(poseStack.last().pose()),
                previous == null ? null : previous.boneIssue()));
    }

    /** CosmeticGeoRenderer, after drawing. Null if the bones looked fine */
    public static void bones(LivingEntity entity, ItemStack stack, String issue) {
        String key = key(entity, CosmeticHelper.getCosmeticId(stack));
        Draw draw = LAST_DRAWN.get(key);
        if (draw != null && !Objects.equals(draw.boneIssue(), issue)) {
            LAST_DRAWN.put(key, new Draw(draw.at(), draw.finitePose(), issue));
        }
    }

    // ---- Every client tick ----

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null) {
            lastLevel = null;
            lastPlayerId = -1;
            return;
        }
        trackEvents(minecraft, player);
        CosmeticStressTest.tick(minecraft);

        if (player.tickCount % 20 == 0) watch(minecraft, player);
        if (player.tickCount % 1200 == 0) {
            long cutoff = Util.getMillis() - 60_000;
            LAST_DRAWN.values().removeIf(draw -> draw.at() < cutoff);
        }
    }

    /** Fires when cosmetics that should be drawn on you haven't been for a few seconds, in third person */
    private static void watch(Minecraft minecraft, LocalPlayer player) {
        long now = Util.getMillis();
        // In first person (and while paused) your model isn't drawn, so nothing to compare
        if (minecraft.options.getCameraType().isFirstPerson() || minecraft.isPaused() || now - playerDrawnAt > 500) return;
        if (player.isInvisible()) return;

        List<String> missing = new ArrayList<>();
        int[] expected = {0};
        forEachWorn(player, (where, stack, info, drawn) -> {
            ResourceLocation id = CosmeticHelper.getCosmeticId(stack);
            if (!drawn || id == null || CosmeticDataLoader.getDefinition(id) == null) return;
            expected[0]++;
            Draw draw = LAST_DRAWN.get(key(player, id));
            if (draw == null || now - draw.at() > MISSING_AFTER_MS) missing.add(id.toString());
        });

        String state = containerState(player);
        if (state.contains("BROKEN")) missing.add("accessories containers " + state);

        if (missing.isEmpty()) {
            if (incident) {
                incident = false;
                event("cosmetics drawing again");
                dump("Cosmetics are drawing again", CosmeticStressTest.running());
            }
            badChecks = 0;
        } else if (++badChecks >= 3 && !incident) {
            incident = true;
            event("cosmetics stopped drawing: " + missing);
            // Normal play only logs it, so players aren't bothered
            boolean testing = CosmeticStressTest.running();
            String during = testing ? " during " + CosmeticStressTest.current() : "";
            dump("Cosmetics stopped drawing" + during + ": " + missing.size() + " of " + expected[0] + " " + missing, testing);
            if (testing) {
                CosmeticStressTest.stop("cosmetics stopped drawing" + during);
                chat("Caught it. Details are in latest.log, search for piscosmetics_debug");
            }
        }
    }

    private static void trackEvents(Minecraft minecraft, LocalPlayer player) {
        String camera = minecraft.options.getCameraType().name();
        if (!camera.equals(lastCamera)) {
            if (lastCamera != null) event("camera " + camera);
            lastCamera = camera;
        }
        String screen = minecraft.screen == null ? "none" : minecraft.screen.getClass().getName();
        if (!screen.equals(lastScreen)) {
            if (lastScreen != null) event("screen " + screen);
            lastScreen = screen;
        }
        String pose = player.getPose().name();
        if (!pose.equals(lastPose)) {
            if (lastPose != null) event("pose " + pose);
            lastPose = pose;
        }
        if (player.getId() != lastPlayerId) {
            if (lastPlayerId != -1) event("player entity replaced, id " + lastPlayerId + " -> " + player.getId() + " (respawn or dimension change)");
            lastPlayerId = player.getId();
        }
        if (minecraft.level != lastLevel) {
            if (lastLevel != null) event("level changed to " + minecraft.level.dimension().location());
            lastLevel = minecraft.level;
        }
        if (player.isPassenger() != lastRiding) {
            lastRiding = player.isPassenger();
            event(lastRiding ? "started riding" : "stopped riding");
        }
        if (player.isFallFlying() != lastGliding) {
            lastGliding = player.isFallFlying();
            event(lastGliding ? "started gliding" : "stopped gliding");
        }
        // What's worn, so an emptied or swapped container shows up
        if (player.tickCount % 5 != 0) return;
        StringBuilder worn = new StringBuilder();
        forEachWorn(player, (where, stack, info, drawn) -> {
            ResourceLocation id = CosmeticHelper.getCosmeticId(stack);
            if (id != null) worn.append(where).append('=').append(id).append(drawn ? "" : "(hidden)").append(' ');
        });
        String state = containerState(player);
        if (!state.equals(lastContainerState)) {
            if (lastContainerState != null) event("accessories containers now " + state);
            lastContainerState = state;
        }
        String wornNow = worn.toString().trim();
        if (!wornNow.equals(lastWorn)) {
            if (lastWorn != null) event("worn now: " + (wornNow.isEmpty() ? "nothing" : wornNow));
            lastWorn = wornNow;
        }
    }

    public static void event(String text) {
        EVENTS.addLast(LocalTime.now().withNano(0) + " " + text);
        while (EVENTS.size() > MAX_EVENTS) EVENTS.removeFirst();
    }

    // ---- Report ----

    private static void dump(String title, boolean toChat) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) return;

        List<String> lines = new ArrayList<>();
        lines.add(title);
        lines.add("Definitions: " + CosmeticDataLoader.getAllDefinitions().size()
                + ", render_for items: " + CosmeticDataLoader.standInCount());
        lines.add("Camera " + minecraft.options.getCameraType() + ", screen "
                + (minecraft.screen == null ? "none" : minecraft.screen.getClass().getName())
                + ", pose " + player.getPose() + ", riding " + player.isPassenger()
                + ", invisible " + player.isInvisible() + ", model last drawn "
                + (Util.getMillis() - playerDrawnAt) + "ms ago");
        report(player, lines);
        if (minecraft.crosshairPickEntity instanceof LivingEntity target && target != player) report(target, lines);
        lines.add("Recent events:");
        lines.addAll(EVENTS);

        for (String line : lines) LOGGER.info("[piscosmetics_debug] {}", line);
        if (minecraft.hasSingleplayerServer()) reportServerSide(minecraft, player.getUUID(), toChat);
        if (!toChat) return;
        // Chat gets the summary, the log gets everything
        int shown = Math.min(lines.size(), lines.indexOf("Recent events:"));
        for (int i = 0; i < shown; i++) chat(lines.get(i));
    }

    /** Singleplayer: what the built-in server thinks you wear, read on its own thread. Lost client data vs not drawn */
    private static void reportServerSide(Minecraft minecraft, UUID playerId, boolean toChat) {
        IntegratedServer server = minecraft.getSingleplayerServer();
        if (server == null) return;
        server.execute(() -> {
            ServerPlayer serverPlayer = server.getPlayerList().getPlayer(playerId);
            if (serverPlayer == null) return;
            List<String> worn = new ArrayList<>();
            forEachWorn(serverPlayer, (where, stack, info, drawn) -> {
                ResourceLocation id = CosmeticHelper.getCosmeticId(stack);
                if (id != null) worn.add(where + "=" + id);
            });
            String line = "Server side: accessories " + containerState(serverPlayer) + ", wearing "
                    + (worn.isEmpty() ? "nothing" : String.join(" ", worn));
            LOGGER.info("[piscosmetics_debug] {}", line);
            if (toChat) minecraft.execute(() -> chat(line));
        });
    }

    private static void report(LivingEntity entity, List<String> lines) {
        MovementState.State move = MovementState.get(entity);
        lines.add("== " + entity.getName().getString() + " #" + entity.getId()
                + String.format(" forward=%.2f strafe=%.2f glide=%s ground=%s",
                move.forward, move.strafe, move.gliding, move.grounded));
        int slotTypes = forEachWorn(entity, (where, stack, info, drawn) -> {
            if (CosmeticHelper.getCosmeticId(stack) != null) lines.add(describe(where, entity, stack, info));
        });
        if (Services.PLATFORM.isModLoaded("accessories")) lines.add("accessories: " + containerState(entity) + ", " + slotTypes + " slot types listed");
    }

    /** Vanilla armor slots, then Accessories. Returns Accessories' slot type count, -1 without a capability, 0 without Accessories */
    private static int forEachWorn(LivingEntity entity, WornVisitor visitor) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) continue;
            ItemStack stack = entity.getItemBySlot(slot);
            if (stack.isEmpty()) continue;
            // Only these are drawn by CosmeticArmorRenderMixin
            boolean drawn = stack.getItem() instanceof CosmeticArmorItem || CosmeticSlotNames.isUniversal(stack);
            visitor.visit("vanilla " + slot.getName(), stack, "", drawn);
        }
        return Services.PLATFORM.isModLoaded("accessories") ? AccessoriesCompat.forEachWorn(entity, visitor) : 0;
    }

    private static String containerState(LivingEntity entity) {
        return Services.PLATFORM.isModLoaded("accessories") ? AccessoriesCompat.containerState(entity) : "no accessories";
    }

    private static String describe(String where, LivingEntity entity, ItemStack stack, String info) {
        ResourceLocation look = CosmeticHelper.getCosmeticId(stack);
        ResourceLocation disguised = CosmeticHelper.getDisguisedItemId(stack);
        StringBuilder line = new StringBuilder(where).append(": ").append(BuiltInRegistries.ITEM.getKey(stack.getItem()));
        if (disguised != null) line.append(" (really ").append(disguised).append(')');
        line.append(" -> ").append(look == null ? "no cosmetic" : look);
        if (look != null && CosmeticDataLoader.getDefinition(look) == null) line.append(" [NO DEFINITION]");
        line.append(info);
        if (look == null) return line.toString();

        Draw draw = LAST_DRAWN.get(key(entity, look));
        if (draw == null) {
            line.append(" | never drawn");
        } else {
            line.append(" | drawn ").append(Util.getMillis() - draw.at()).append("ms ago");
            if (!draw.finitePose()) line.append(" [BAD POSE]");
            if (draw.boneIssue() != null) line.append(" [").append(draw.boneIssue()).append(']');
        }
        return line.toString();
    }

    private static void chat(String text) {
        Minecraft.getInstance().gui.getChat().addMessage(Component.literal(text).withStyle(ChatFormatting.GRAY));
    }

    private static String key(LivingEntity entity, ResourceLocation cosmeticId) {
        return entity.getId() + "/" + cosmeticId;
    }

    private static boolean isFinite(Matrix4f matrix) {
        matrix.get(MATRIX);
        for (float value : MATRIX) {
            if (!Float.isFinite(value)) return false;
        }
        return true;
    }
}
