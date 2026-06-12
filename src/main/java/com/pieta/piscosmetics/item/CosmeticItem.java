package com.pieta.piscosmetics.item;

import com.pieta.piscosmetics.api.CosmeticDefinition;
import com.pieta.piscosmetics.client.CosmeticClientAnimations;
import com.pieta.piscosmetics.client.renderer.CosmeticGeoRenderer;
import com.pieta.piscosmetics.data.CosmeticDataLoader;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
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

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        CosmeticGeoRenderer[] rendererHolder = new CosmeticGeoRenderer[1];
        rendererHolder[0] = new CosmeticGeoRenderer(
                new DefaultedItemGeoModel<CosmeticItem>(
                        BuiltInRegistries.ITEM.getKey(CosmeticItem.this)
                ) {
                    private CosmeticDefinition getDef() {
                        ItemStack stack = rendererHolder[0].getCurrentItemStack();
                        return CosmeticHelper.getDefinition(stack);
                    }

                    @Override
                    public ResourceLocation getModelResource(CosmeticItem animatable) {
                        return CosmeticHelper.getModelResource(animatable, getDef(), super.getModelResource(animatable));
                    }

                    @Override
                    public ResourceLocation getTextureResource(CosmeticItem animatable) {
                        return CosmeticHelper.getTextureResource(animatable, getDef(), super.getTextureResource(animatable));
                    }

                    @Override
                    public ResourceLocation getAnimationResource(CosmeticItem animatable) {
                        return CosmeticHelper.getAnimationResource(animatable, getDef(), super.getAnimationResource(animatable));
                    }
                }
        );

        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return rendererHolder[0];
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