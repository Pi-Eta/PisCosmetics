package com.pieta.piscosmetics.client;

import com.pieta.piscosmetics.item.CosmeticItem;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;

public final class CosmeticClientAnimations {

    public static void registerControllers(
            CosmeticItem item,
            AnimatableManager.ControllerRegistrar controllers
    ) {
        controllers.add(new AnimationController<>(
                item,
                "movement_controller",
                10,
                state -> {

                    var player = net.minecraft.client.Minecraft.getInstance().player;

                    if (player == null) {
                        return state.setAndContinue(
                                RawAnimation.begin().thenLoop("idle")
                        );
                    }

                    var ms = MovementState.get(player.getUUID());
                    var result = AnimationResolver.resolve(ms, player);

                    return state.setAndContinue(
                            RawAnimation.begin().thenLoop(result.baseAnimation())
                    );
                }
        ));
    }
}