package net.hmmer.infection.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import org.jspecify.annotations.Nullable;


public class CandyCornParticle extends SingleQuadParticle {

    private final SpriteSet sprites;

    private CandyCornParticle(final ClientLevel level, final double x, final double y, final double z,
                              final SpriteSet sprites, RandomSource random) {
        super(level, x, y, z, sprites.get(random));
        this.sprites = sprites;

        // Pick a random direction around the circle and a random strength
        double angle = random.nextDouble() * Math.PI * 2;
        double outward = 0.05 + random.nextDouble() * 0.1;   // horizontal speed

        this.xd = Math.cos(angle) * outward;
        this.zd = Math.sin(angle) * outward;
        this.yd = 0.1 + random.nextDouble() * 0.2; // goes up

        this.gravity = 0.75F;
        this.friction = 0.999F;
        this.hasPhysics = true;

        this.lifetime = 60;

        this.setSpriteFromAge(sprites);

    }

    // The layer type of the particle, it is translucent so it can fade out.
    @Override
    protected Layer getLayer() {
        return Layer.TRANSLUCENT;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        float progress = (float)this.age / (float)this.lifetime; // goes from 0 to 1 over its lifetime

        if (progress > 0.5f) {
            float fade = (progress - 0.5f) * 2f;
            this.setAlpha(1.0F - fade);
        }
        // Slowly fade away over its lifetime


        this.setSpriteFromAge(sprites); // Makes the particle go through its animation frames

    }

    // Creates the particle when one is spawned in
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public @Nullable Particle createParticle(SimpleParticleType options, ClientLevel level,
                                                 double x, double y, double z,
                                                 double xAux, double yAux, double zAux,
                                                 RandomSource random) {
            return new CandyCornParticle(level, x, y, z, sprites, random);
        }
    }
}
