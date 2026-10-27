package com.tvorozhoc.leftarm.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.tvorozhoc.leftarm.ParticleColors;
import net.minecraft.client.renderer.LevelEventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Событие 2013 — удар булавой с высоты: помечаем частицы, которые сейчас появятся. */
@Mixin(LevelEventHandler.class)
public class LevelEventHandlerMixin {
	@WrapOperation(method = "levelEvent", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/util/ParticleUtils;spawnSmashAttackParticles(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;I)V"))
	private void leftarm$markMaceParticles(LevelAccessor level, BlockPos pos, int count, Operation<Void> original) {
		ParticleColors.spawningMaceParticles = true;
		try {
			original.call(level, pos, count);
		} finally {
			ParticleColors.spawningMaceParticles = false;
		}
	}
}
