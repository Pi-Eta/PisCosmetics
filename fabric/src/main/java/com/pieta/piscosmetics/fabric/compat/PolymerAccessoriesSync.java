package com.pieta.piscosmetics.fabric.compat;

import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import io.wispforest.accessories.endec.NbtMapCarrier;
import io.wispforest.accessories.networking.AccessoriesNetworking;
import io.wispforest.accessories.networking.client.SyncEntireContainer;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Accessories' full sync is NBT built before sending, so Polymer never disguises it and clients drop
 * server-only items. Sends a disguised copy per player instead. Only called with Polymer installed
 */
public final class PolymerAccessoriesSync {

    // Lets our own copies through the mixin
    private static boolean sending = false;

    private PolymerAccessoriesSync() {}

    /** False if the original should be sent */
    public static boolean sendToPlayer(ServerPlayer target, SyncEntireContainer packet) {
        if (sending) return false;
        send(List.of(target), packet);
        return true;
    }

    public static boolean sendToTrackingAndSelf(Entity entity, SyncEntireContainer packet) {
        if (sending) return false;
        Set<ServerPlayer> targets = new LinkedHashSet<>(PlayerLookup.tracking(entity));
        if (entity instanceof ServerPlayer self) targets.add(self);
        send(targets, packet);
        return true;
    }

    private static void send(Collection<ServerPlayer> targets, SyncEntireContainer packet) {
        sending = true;
        try {
            for (ServerPlayer target : targets) {
                CompoundTag tag = packet.containerMap().compoundTag().copy();
                disguise(tag, target);
                AccessoriesNetworking.sendToPlayer(target, new SyncEntireContainer(packet.entityId(), new NbtMapCarrier(tag)));
            }
        } finally {
            sending = false;
        }
    }

    /** Walks the whole tag, independent of Accessories' layout */
    private static void disguise(Tag tag, ServerPlayer player) {
        if (tag instanceof CompoundTag compound) {
            for (String key : new ArrayList<>(compound.getAllKeys())) {
                Tag child = compound.get(key);
                if (child instanceof CompoundTag stackTag && isStack(stackTag)) {
                    compound.put(key, disguiseStack(stackTag, player));
                } else {
                    disguise(child, player);
                }
            }
        } else if (tag instanceof ListTag list) {
            for (int i = 0; i < list.size(); i++) {
                Tag child = list.get(i);
                if (child instanceof CompoundTag stackTag && isStack(stackTag)) {
                    list.set(i, disguiseStack(stackTag, player));
                } else {
                    disguise(child, player);
                }
            }
        }
    }

    // A saved ItemStack: {id: "mod:item", count: 1, components: {...}}
    private static boolean isStack(CompoundTag tag) {
        if (!tag.contains("id", Tag.TAG_STRING) || !tag.contains("count", Tag.TAG_ANY_NUMERIC)) return false;
        ResourceLocation id = ResourceLocation.tryParse(tag.getString("id"));
        return id != null && BuiltInRegistries.ITEM.containsKey(id);
    }

    private static CompoundTag disguiseStack(CompoundTag tag, ServerPlayer player) {
        HolderLookup.Provider registries = player.registryAccess();
        Optional<ItemStack> parsed = ItemStack.parse(registries, tag);
        if (parsed.isEmpty()) return tag;

        ItemStack real = parsed.get();
        ItemStack shown = PolymerItemUtils.getPolymerItemStack(real, registries, player);
        if (shown == real || shown.isEmpty()) return tag;

        // Keeps the slot number
        CompoundTag extra = tag.copy();
        extra.remove("id");
        extra.remove("count");
        extra.remove("components");
        return (CompoundTag) shown.save(registries, extra);
    }
}
