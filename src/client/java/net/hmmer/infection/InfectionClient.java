package net.hmmer.infection;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.hmmer.infection.commands.ModClientCommands;
import net.hmmer.infection.network.OpenMenuPayload;
import net.hmmer.infection.particles.CandyCornParticle;
import net.hmmer.infection.particles.ModParticles;
import net.hmmer.infection.screen.InfectMenuScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class InfectionClient implements ClientModInitializer {

    // Mod ID
    public static final String MOD_ID = "infection";

    // To log what each thing does
    public static final Logger LOGGER = LoggerFactory.getLogger( MOD_ID + " Client");

    @Override
    public void onInitializeClient() {
        //=======================================================
        // Creates a new log saying infection will now register its assets for client
        LOGGER.info("[Infection] registering all assets for InfectionMod");

        // Registering
        ModClientCommands.registerClientCommands(); // Registers Client Commands

        ParticleProviderRegistry. // Register the particle instance for CANDY_CORN Particle behavior
                getInstance().register(ModParticles.CANDY_CORN, CandyCornParticle.Provider::new);

        //=======================================================
        // Receiving Payloads

        ClientPlayNetworking. // Runs when receives OpenMenuPayload
                registerGlobalReceiver(OpenMenuPayload.TYPE, (payload, context) -> {
                    context.client().setScreenAndShow(new InfectMenuScreen());
        });
    }
}
