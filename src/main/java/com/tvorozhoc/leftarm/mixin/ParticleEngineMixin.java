package com.tvorozhoc.leftarm.mixin;

import com.tvorozhoc.leftarm.ParticleColors;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {
	@Inject(method = "createParticle", at = @At("RETURN"))
	private void leftarm$recolorMace(ParticleOptions options, double x, double y, double z, double dx, double dy, double dz,
									 CallbackInfoReturnable<Particle> cir) {
		Particle particle = cir.getReturnValue();
		if (particle != null && ParticleColors.spawningMaceParticles) {
			ParticleColors.applyMace(particle);
		}
	}
}
