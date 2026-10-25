package net.hmmer.infection;

import net.fabricmc.api.ModInitializer;

import net.hmmer.infection.creativemodtabs.ModCreativeModeTabs;
import net.hmmer.infection.items.ModItems;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Infection implements ModInitializer {
    // Mod ID
	public static final String MOD_ID = "infection";
    // To log what each thing does
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
        // registers creative mode tabs, have this at the top
        ModCreativeModeTabs.registerCreativeTabs();

        // registers items
        ModItems.registerItems();
	}
}
