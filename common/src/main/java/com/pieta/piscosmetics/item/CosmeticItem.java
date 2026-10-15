package com.pieta.piscosmetics.item;

import com.pieta.piscosmetics.client.CosmeticClientAnimations;
import com.pieta.piscosmetics.client.renderer.CosmeticGeoRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class CosmeticItem extends Item implements GeoItem, ICosmeticItem {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final Map<String, RawAnimation> animationCache = new HashMap<>();

    private RawAnimation getAnimation(String name) {
        return animationCache.computeIfAbsent(name, key -> RawAnimation.begin().thenLoop(key));
    }

    public CosmeticItem(Properties properties) {
        super(properties);
    }

    /**
     * GeckoLib method to force custom renderer onto both loaders' items
     */
    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private CosmeticGeoRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
                if (this.renderer == null) {
                    this.renderer = CosmeticGeoRenderer.create(CosmeticItem.this);
                }
                return this.renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        CosmeticClientAnimations.registerControllers(this, controllers);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public net.minecraft.network.chat.Component getName(ItemStack stack) {
        return CosmeticHelper.getName(stack, super.getName(stack));
    }
}