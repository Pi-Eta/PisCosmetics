package com.pieta.piscosmetics.item;

import com.pieta.piscosmetics.api.CosmeticSlotNames;
import com.pieta.piscosmetics.client.CosmeticClientAnimations;
import com.pieta.piscosmetics.client.renderer.CosmeticGeoRenderer;
import com.pieta.piscosmetics.platform.Services;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.HashMap;
import java.util.List;
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

    /**
     * Right-click puts armor-slot cosmetics on
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        EquipmentSlot slot = CosmeticSlotNames.armorSlot(CosmeticSlotNames.slotKey(stack));
        if (slot == null || !player.canUseSlot(slot)) return super.use(level, player, hand);

        ItemStack worn = player.getItemBySlot(slot);
        boolean stuck = EnchantmentHelper.has(worn, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE) && !player.isCreative();
        if (stuck || ItemStack.matches(stack, worn)) return InteractionResultHolder.fail(stack);

        if (!level.isClientSide()) player.awardStat(Stats.ITEM_USED.get(this));
        ItemStack toHand = worn.isEmpty() ? stack : worn.copyAndClear();
        ItemStack toWear = player.isCreative() ? stack.copy() : stack.copyAndClear();
        player.setItemSlot(slot, toWear);
        return InteractionResultHolder.sidedSuccess(toHand, level.isClientSide());
    }

    /** Refuse other slots without Accessories */
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        if (!CosmeticHelper.canBeWornWithoutAccessories(stack) && !Services.PLATFORM.isModLoaded("accessories")) {
            tooltip.add(Component.translatable("tooltip.piscosmetics.needs_accessories").withStyle(ChatFormatting.GRAY));
        }
    }
}