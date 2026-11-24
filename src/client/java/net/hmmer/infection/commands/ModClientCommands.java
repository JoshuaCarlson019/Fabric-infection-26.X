package net.hmmer.infection.commands;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.hmmer.infection.InfectionClient;
import net.minecraft.client.gui.screens.Screen;

// Commands that can run on the client
public class ModClientCommands {

    private static Screen pendingScreen = null;

    public static void registerClientCommands() {
        InfectionClient.LOGGER.info("[Infection] Registering client commands for " + InfectionClient.MOD_ID);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (pendingScreen != null) {
                client.setScreenAndShow(pendingScreen);
                pendingScreen = null;
            }
        });
    }
}
