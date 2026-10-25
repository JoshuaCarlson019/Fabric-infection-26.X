package net.hmmer.infection.creativemodtabs;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.hmmer.infection.Infection;
import net.hmmer.infection.items.ModItems;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModCreativeModeTabs {
    public static final CreativeModeTab infectionItemTab = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath(Infection.MOD_ID, "infection_items"),
            FabricCreativeModeTab.builder().icon(() -> new ItemStack(ModItems.goodiesBag))
                            .title(Component.translatable("CreativeModeTab.infection_items"))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.goodiesBag);


                    }).build());

    public static final CreativeModeTab infectionBlockTab = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath(Infection.MOD_ID, "infection_blocks"),
            FabricCreativeModeTab.builder().icon(() -> new ItemStack(ModItems.goodiesBag))
                    .title(Component.translatable("CreativeModeTab.infection_blocks"))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.goodiesBag);


                    }).build());

    public static void registerCreativeTabs() {
        // logs it to put out if there is an error
        Infection.LOGGER.info("Registering Creative Mode Tabs for " + Infection.MOD_ID);
    }
}
