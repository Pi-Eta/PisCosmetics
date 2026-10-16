package com.pieta.piscosmetics.item;

import com.pieta.piscosmetics.ModItems;
import com.pieta.piscosmetics.PisCosmetics;
import com.pieta.piscosmetics.api.CosmeticDefinition;
import com.pieta.piscosmetics.data.CosmeticDataLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;

import java.util.List;

/**
 * Smithing template for BlueprintSmithingRecipe. The cosmetic is stored under "blueprint", not "cosmetic",
 * so a blueprint is never treated as a worn cosmetic
 */
public class CosmeticBlueprintItem extends Item {

    /** What wearable cosmetics are made from */
    public static final TagKey<Item> BASE = TagKey.create(Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath(PisCosmetics.MODID, "blueprint_base"));

    public CosmeticBlueprintItem(Properties properties) {
        super(properties);
    }

    /** A blueprint for this cosmetic */
    public static ItemStack create(ResourceLocation cosmeticId) {
        ItemStack stack = new ItemStack(ModItems.COSMETIC_BLUEPRINT);
        CompoundTag tag = new CompoundTag();
        tag.putString("blueprint", cosmeticId.toString());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return stack;
    }

    /** No cosmetic set, crafted with recipe/blank_blueprint.json */
    public static boolean isBlank(ItemStack stack) {
        return stack.getItem() instanceof CosmeticBlueprintItem
                && stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).getUnsafe().getString("blueprint").isEmpty();
    }

    /** The blueprint's cosmetic, or null if unset or missing */
    public static ResourceLocation getBlueprint(ItemStack blueprint) {
        String id = blueprint.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).getUnsafe().getString("blueprint");
        ResourceLocation cosmeticId = id.isEmpty() ? null : ResourceLocation.tryParse(id);
        return cosmeticId != null && CosmeticDataLoader.getDefinition(cosmeticId) != null ? cosmeticId : null;
    }

    /** False if the cosmetic or this blueprint's custom data has copyable: false */
    public static boolean canCopy(ItemStack blueprint) {
        ResourceLocation cosmeticId = getBlueprint(blueprint);
        if (cosmeticId == null || !CosmeticDataLoader.getDefinition(cosmeticId).copyable()) return false;
        CompoundTag data = blueprint.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).getUnsafe();
        return !data.contains("copyable") || data.getBoolean("copyable");
    }

    @Override
    public Component getName(ItemStack stack) {
        if (isBlank(stack)) return Component.translatable("item.piscosmetics.cosmetic_blueprint.blank").withStyle(ChatFormatting.BLUE);
        ResourceLocation cosmeticId = getBlueprint(stack);
        if (cosmeticId == null) return super.getName(stack);
        // Cosmetic name keeps its own colour
        return Component.translatable("item.piscosmetics.cosmetic_blueprint.named", CosmeticHelper.displayName(cosmeticId))
                .withStyle(ChatFormatting.BLUE);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        if (isBlank(stack)) {
            tooltip.add(Component.translatable("tooltip.piscosmetics.blueprint.blank").withStyle(ChatFormatting.GRAY));
            return;
        }
        ResourceLocation cosmeticId = getBlueprint(stack);
        if (cosmeticId == null) {
            tooltip.add(Component.translatable("tooltip.piscosmetics.blueprint.empty").withStyle(ChatFormatting.RED));
            return;
        }
        CosmeticDefinition def = CosmeticDataLoader.getDefinition(cosmeticId);
        if (def.isKeyItemLook()) {
            // e.g. "Mega or Tera"
            MutableComponent types = Component.empty();
            for (int i = 0; i < def.keyItems().size(); i++) {
                if (i > 0) types.append(Component.translatable(i == def.keyItems().size() - 1
                        ? "tooltip.piscosmetics.blueprint.or" : "tooltip.piscosmetics.blueprint.comma"));
                types.append(Component.translatable("key_item.piscosmetics." + def.keyItems().get(i)).withStyle(ChatFormatting.YELLOW));
            }
            tooltip.add(Component.translatable("tooltip.piscosmetics.blueprint.key_items", types).withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.translatable("tooltip.piscosmetics.blueprint.wearable", baseName().copy().withStyle(ChatFormatting.GREEN))
                    .withStyle(ChatFormatting.GRAY));
        }
        if (!canCopy(stack)) {
            tooltip.add(Component.translatable("tooltip.piscosmetics.blueprint.uncopyable").withStyle(ChatFormatting.DARK_RED));
        }
    }

    // First item in the blueprint_base tag
    private static Component baseName() {
        return BuiltInRegistries.ITEM.getTag(BASE)
                .filter(set -> set.size() > 0)
                .map(set -> set.get(0).value().getDescription())
                .orElse(Component.translatable("tooltip.piscosmetics.blueprint.base_unset"));
    }
}
