package com.tvorozhoc.leftarm;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SingleQuadParticle;

import java.util.concurrent.ThreadLocalRandom;

/** Перекраска частиц тотема и булавы. */
public final class ParticleColors {
	/** true, пока игра спавнит частицы удара булавой (событие 2013). */
	public static boolean spawningMaceParticles;

	private ParticleColors() {
	}

	public static void applyTotem(Particle particle) {
		LeftArmConfig config = LeftArmConfig.get();
		if (config.totemParticlesCustom) {
			apply(particle, LeftArmConfig.parseColor(config.totemParticlesColor));
		}
	}

	public static void applyMace(Particle particle) {
		LeftArmConfig config = LeftArmConfig.get();
		if (config.maceParticlesCustom) {
			apply(particle, LeftArmConfig.parseColor(config.maceParticlesColor));
		}
	}

	private static void apply(Particle particle, int color) {
		if (color < 0 || !(particle instanceof SingleQuadParticle quad)) {
			return;
		}
		// Небольшой разброс яркости, чтобы частицы не сливались в одно пятно
		float shade = 0.8f + ThreadLocalRandom.current().nextFloat() * 0.2f;
		quad.setColor(
				(color >> 16 & 0xFF) / 255f * shade,
				(color >> 8 & 0xFF) / 255f * shade,
				(color & 0xFF) / 255f * shade);
	}
}
