package com.pieta.piscosmetics.recipe;

import com.mojang.serialization.MapCodec;
import com.pieta.piscosmetics.ModRecipes;
import com.pieta.piscosmetics.api.CosmeticDefinition;
import com.pieta.piscosmetics.api.KeyItemTypes;
import com.pieta.piscosmetics.data.CosmeticDataLoader;
import com.pieta.piscosmetics.item.CosmeticBlueprintItem;
import com.pieta.piscosmetics.item.CosmeticHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.Level;

/**
 * Blueprint as template, empty addition. Base decides the result:
 * - blank blueprint: two copies, unless uncopyable
 * - key item of the cosmetic's key_items types: the item with the look
 * - blueprint_base item: the wearable cosmetic (not for key item looks)
 * Disguised items never count as a base
 */
public class BlueprintSmithingRecipe implements SmithingRecipe {

    public static final BlueprintSmithingRecipe INSTANCE = new BlueprintSmithingRecipe();

    // Blanks go in the base slot, including on shift-click
    @Override
    public boolean isTemplateIngredient(ItemStack stack) {
        return stack.getItem() instanceof CosmeticBlueprintItem && !CosmeticBlueprintItem.isBlank(stack);
    }

    // What the base slot accepts at all, matches() checks the rest
    @Override
    public boolean isBaseIngredient(ItemStack stack) {
        return KeyItemTypes.isOneOf(stack, KeyItemTypes.ALL) || isBlueprintBase(stack) || CosmeticBlueprintItem.isBlank(stack);
    }

    @Override
    public boolean isAdditionIngredient(ItemStack stack) {
        return false;
    }

    @Override
    public boolean matches(SmithingRecipeInput input, Level level) {
        if (!isTemplateIngredient(input.template()) || !input.addition().isEmpty()) return false;
        ResourceLocation cosmeticId = CosmeticBlueprintItem.getBlueprint(input.template());
        if (cosmeticId == null) return false;
        CosmeticDefinition def = CosmeticDataLoader.getDefinition(cosmeticId);
        ItemStack base = input.base();

        if (CosmeticBlueprintItem.isBlank(base)) return CosmeticBlueprintItem.canCopy(input.template());
        if (def.isKeyItemLook()) {
            // Not if it already has this look
            return KeyItemTypes.isOneOf(base, def.keyItems()) && !cosmeticId.toString().equals(currentLook(base));
        }
        return isBlueprintBase(base);
    }

    @Override
    public ItemStack assemble(SmithingRecipeInput input, HolderLookup.Provider registries) {
        ResourceLocation cosmeticId = CosmeticBlueprintItem.getBlueprint(input.template());
        if (cosmeticId == null) return ItemStack.EMPTY;
        CosmeticDefinition def = CosmeticDataLoader.getDefinition(cosmeticId);

        // Template is used up, so the original plus a copy
        if (CosmeticBlueprintItem.isBlank(input.base())) return input.template().copyWithCount(2);
        if (def.isKeyItemLook()) {
            ItemStack result = input.base().copyWithCount(1);
            CustomData.update(DataComponents.CUSTOM_DATA, result, tag -> tag.putString("cosmetic", cosmeticId.toString()));
            return result;
        }
        // Universal item
        return CosmeticHelper.displayStack(cosmeticId).copy();
    }

    private static boolean isBlueprintBase(ItemStack stack) {
        return stack.is(CosmeticBlueprintItem.BASE) && !CosmeticHelper.isDisguised(stack);
    }

    private static String currentLook(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).getUnsafe().getString("cosmetic");
    }

    // Depends on inputs
    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    // Hidden from the recipe book
    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.APPLY_BLUEPRINT;
    }

    /** No settings: {"type": "piscosmetics:apply_blueprint"} */
    public static final class Serializer implements RecipeSerializer<BlueprintSmithingRecipe> {
        private static final MapCodec<BlueprintSmithingRecipe> CODEC = MapCodec.unit(INSTANCE);
        private static final StreamCodec<RegistryFriendlyByteBuf, BlueprintSmithingRecipe> STREAM_CODEC = StreamCodec.unit(INSTANCE);

        @Override
        public MapCodec<BlueprintSmithingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, BlueprintSmithingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
