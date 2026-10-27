package com.tvorozhoc.leftarm.mixin;

import com.tvorozhoc.leftarm.ParticleColors;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.TotemParticle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Ванильный конструктор красит частицу в случайный зелёный/жёлтый — перекрашиваем после него. */
@Mixin(TotemParticle.class)
public class TotemParticleMixin {
	@Inject(method = "<init>", at = @At("TAIL"))
	private void leftarm$recolor(CallbackInfo ci) {
		ParticleColors.applyTotem((Particle) (Object) this);
	}
}
