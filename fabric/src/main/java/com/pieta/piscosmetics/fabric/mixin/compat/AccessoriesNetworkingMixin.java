package com.pieta.piscosmetics.fabric.mixin.compat;

import com.pieta.piscosmetics.fabric.compat.PolymerAccessoriesSync;
import io.wispforest.accessories.networking.client.SyncEntireContainer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Desc;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Every full Accessories sync goes out through one of these two methods. With Polymer installed,
 * PolymerAccessoriesSync sends its own per-player copies instead, with server-only items disguised.
 * <p>
 * Pseudo and require = 0 like AccessoriesRendererRegistryMixin: skipped without Accessories, and the game
 * still starts if Accessories changes these methods.
 */
@Pseudo
@Mixin(targets = "io.wispforest.accessories.networking.AccessoriesNetworking", remap = false)
public abstract class AccessoriesNetworkingMixin {

    @Inject(target = @Desc(value = "sendToPlayer", args = {ServerPlayer.class, Record.class}),
            at = @At("HEAD"), cancellable = true, require = 0)
    private static void piscosmetics$sendToPlayer(ServerPlayer player, Record packet, CallbackInfo ci) {
        if (packet instanceof SyncEntireContainer sync && FabricLoader.getInstance().isModLoaded("polymer-core")
                && PolymerAccessoriesSync.sendToPlayer(player, sync)) {
            ci.cancel();
        }
    }

    @Inject(target = @Desc(value = "sendToTrackingAndSelf", args = {Entity.class, Record.class}),
            at = @At("HEAD"), cancellable = true, require = 0)
    private static void piscosmetics$sendToTrackingAndSelf(Entity entity, Record packet, CallbackInfo ci) {
        if (packet instanceof SyncEntireContainer sync && FabricLoader.getInstance().isModLoaded("polymer-core")
                && PolymerAccessoriesSync.sendToTrackingAndSelf(entity, sync)) {
            ci.cancel();
        }
    }
}
