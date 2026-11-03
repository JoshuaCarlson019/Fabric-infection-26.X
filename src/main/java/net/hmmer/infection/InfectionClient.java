package net.hmmer.infection;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.hmmer.infection.particles.CandyCornParticle;
import net.hmmer.infection.particles.ModParticles;
import net.minecraft.client.particle.LavaParticle;
import net.minecraft.client.particle.SplashParticle;


public class InfectionClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ParticleProviderRegistry.getInstance().register(ModParticles.CANDY_CORN, CandyCornParticle.Provider::new);
    }
}
