package com.pieta.piscosmetics;

import com.pieta.piscosmetics.api.CosmeticDefinition;
import com.pieta.piscosmetics.data.CosmeticDataLoader;
import com.pieta.piscosmetics.item.CosmeticBlueprintItem;
import com.pieta.piscosmetics.item.CosmeticHelper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * Creative tab: blank blueprint, every cosmetic's blueprint, then every wearable cosmetic.
 * Refilled when definitions change (PisCosmeticsClient.onDefinitionsChanged)
 */
public final class ModCreativeTab {

    public static CreativeModeTab TAB;

    /** Custom model data for the π blueprint model (models/item/cosmetic_blueprint.json) */
    public static final int PI_ICON = 314159;

    private ModCreativeTab() {}

    // Tab icon only
    private static ItemStack icon() {
        ItemStack stack = new ItemStack(ModItems.COSMETIC_BLUEPRINT);
        stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(PI_ICON));
        return stack;
    }

    /** Builder comes from the loader, Fabric needs FabricItemGroup.builder() */
    public static void register(Supplier<CreativeModeTab.Builder> builder, BiConsumer<ResourceLocation, CreativeModeTab> registrar) {
        TAB = builder.get()
                .title(Component.translatable("itemGroup.piscosmetics"))
                .icon(ModCreativeTab::icon)
                .displayItems((parameters, output) -> {
                    List<Map.Entry<ResourceLocation, CosmeticDefinition>> cosmetics =
                            new ArrayList<>(CosmeticDataLoader.getAllDefinitions().entrySet());
                    cosmetics.sort(Map.Entry.comparingByKey(Comparator.comparing(ResourceLocation::toString)));

                    output.accept(new ItemStack(ModItems.COSMETIC_BLUEPRINT)); // blank
                    for (var cosmetic : cosmetics) {
                        output.accept(CosmeticBlueprintItem.create(cosmetic.getKey()));
                    }
                    // Key item looks have no item of their own
                    for (var cosmetic : cosmetics) {
                        if (!cosmetic.getValue().isKeyItemLook()) {
                            output.accept(CosmeticHelper.displayStack(cosmetic.getKey()).copy());
                        }
                    }
                })
                .build();
        registrar.accept(ResourceLocation.fromNamespaceAndPath(PisCosmetics.MODID, "cosmetics"), TAB);
    }
}
