package net.hmmer.infection.items;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.hmmer.infection.Infection;
import net.hmmer.infection.items.custom.OpenBagItem;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.ItemLore;

import java.util.List;
import java.util.function.Function;


public class ModItems {
    // Creates the item called goodie Bag with no properties
    public static final Item goodiesBag = registerItem("goodies_bag",
            properties -> new OpenBagItem(properties
                    .stacksTo(16)
                    .rarity(Rarity.RARE)
                    .component(DataComponents.LORE, new ItemLore(List.of(Component
                            .literal("Right click to open me! :)")
                            .withColor(16753920)))), "goodies_bag"));



    /**
     * Registers an Item
     * @param name the name of the item
     * @param function the items properties stored in a function and uses the setId Method
     * @return the item
     */
    private static Item registerItem(String name, Function<Item.Properties, Item> function) {
        return Registry.register(BuiltInRegistries.ITEM,
                Identifier.fromNamespaceAndPath(Infection.MOD_ID, name),
                function.apply(new Item.Properties().setId(ResourceKey.create(Registries.ITEM,
                        Identifier.fromNamespaceAndPath(Infection.MOD_ID, name)))));
    }

    public static void registerItems()
    {
        Infection.LOGGER.info("Registering Items for " + Infection.MOD_ID);
    }

}
