package com.pieta.piscosmetics;

import com.pieta.piscosmetics.recipe.BlueprintSmithingRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.function.BiConsumer;

/**
 * Custom recipe types for the two loaders
 */
public final class ModRecipes {

    public static RecipeSerializer<BlueprintSmithingRecipe> APPLY_BLUEPRINT;

    private ModRecipes() {}

    public static void register(BiConsumer<ResourceLocation, RecipeSerializer<?>> registrar) {
        APPLY_BLUEPRINT = new BlueprintSmithingRecipe.Serializer();
        registrar.accept(ResourceLocation.fromNamespaceAndPath(PisCosmetics.MODID, "apply_blueprint"), APPLY_BLUEPRINT);
    }
}
