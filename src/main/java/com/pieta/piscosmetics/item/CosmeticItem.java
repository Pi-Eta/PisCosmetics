package com.pieta.piscosmetics.item;

import com.pieta.piscosmetics.api.CosmeticDefinition;
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
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

public class CosmeticItem extends Item implements GeoItem {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

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

                        if (stack == null) {
                            return null;
                        }

                        String cosmeticId = stack
                                .getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                                .copyTag()
                                .getString("cosmetic");

                        if (cosmeticId.isEmpty()) {
                            return null;
                        }

                        ResourceLocation id = ResourceLocation.tryParse(cosmeticId);

                        if (id == null) {
                            return null;
                        }

                        return CosmeticDataLoader.getDefinition(id);
                    }

                    @Override
                    public ResourceLocation getModelResource(CosmeticItem animatable) {
                        CosmeticDefinition def = getDef();

                        if (def != null && def.model().isPresent()) {
                            ResourceLocation model = def.model().get();

                            String path = model.getPath();

                            if (!path.endsWith(".geo.json")) {
                                path += ".geo.json";
                            }

                            if (!path.startsWith("geo/")) {
                                path = "geo/" + path;
                            }

                            return ResourceLocation.fromNamespaceAndPath(
                                    model.getNamespace(),
                                    path
                            );
                        }

                        return super.getModelResource(animatable);
                    }

                    @Override
                    public ResourceLocation getTextureResource(CosmeticItem animatable) {
                        CosmeticDefinition def = getDef();

                        if (def != null && def.texture().isPresent()) {
                            ResourceLocation texture = def.texture().get();

                            String path = texture.getPath();

                            if (!path.endsWith(".png")) {
                                path += ".png";
                            }

                            if (!path.startsWith("textures/")) {
                                path = "textures/" + path;
                            }

                            return ResourceLocation.fromNamespaceAndPath(
                                    texture.getNamespace(),
                                    path
                            );
                        }

                        return super.getTextureResource(animatable);
                    }

                    @Override
                    public ResourceLocation getAnimationResource(CosmeticItem animatable) {
                        CosmeticDefinition def = getDef();

                        if (def != null && def.animation().isPresent()) {
                            String anim = def.animation().get();

                            ResourceLocation animId = ResourceLocation.tryParse(anim);

                            if (animId != null) {
                                return ResourceLocation.fromNamespaceAndPath(
                                        animId.getNamespace(),
                                        "animations/" + animId.getPath() + ".animation.json"
                                );
                            }
                        }

                        return ResourceLocation.fromNamespaceAndPath(
                                BuiltInRegistries.ITEM.getKey(animatable).getNamespace(),
                                "animations/" +
                                        BuiltInRegistries.ITEM.getKey(animatable).getPath() +
                                        ".animation.json"
                        );
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
        controllers.add(new AnimationController<>(
                this,
                "idle_controller",
                0,
                state -> state.setAndContinue(
                        RawAnimation.begin().thenLoop("idle")
                )
        ));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public net.minecraft.network.chat.Component getName(ItemStack stack) {

        String cosmeticId = stack
                .getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                .copyTag()
                .getString("cosmetic");

        if (!cosmeticId.isEmpty()) {

            ResourceLocation id = ResourceLocation.tryParse(cosmeticId);

            if (id != null) {

                CosmeticDefinition def = CosmeticDataLoader.getDefinition(id);

                if (def != null && def.name().isPresent()) {
                    return net.minecraft.network.chat.Component.literal(
                            def.name().get()
                    );
                }
            }
        }

        return super.getName(stack);
    }
}