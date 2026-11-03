package net.hmmer.infection.particles;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.hmmer.infection.Infection;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public class ModParticles {
    public static final SimpleParticleType CANDY_CORN =
            registerParticle("candy_corn", FabricParticleTypes.simple());

    public static SimpleParticleType registerParticle(String name, SimpleParticleType type) {
        return Registry.register(BuiltInRegistries.PARTICLE_TYPE,
                Identifier.fromNamespaceAndPath(Infection.MOD_ID, name), type);
    }

    public static void registerParticles() {
        Infection.LOGGER.info("Registering Particles for " + Infection.MOD_ID);
    }
}
