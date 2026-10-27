package com.tvorozhoc.leftarm.mixin;

import com.tvorozhoc.leftarm.LeftArmClient;
import com.tvorozhoc.leftarm.LeftArmConfig;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Set;

/** Подменяет звуки удара булавой на свой (assets/.../sounds/mace_hit.ogg). */
@Mixin(SoundEngine.class)
public class SoundEngineMixin {
	@Unique
	private static final Identifier MACE_HIT = Identifier.fromNamespaceAndPath(LeftArmClient.MOD_ID, "mace_hit");

	@Unique
	private static final Set<Identifier> MACE_SOUNDS = Set.of(
			SoundEvents.MACE_SMASH_GROUND.location(),
			SoundEvents.MACE_SMASH_GROUND_HEAVY.location(),
			SoundEvents.MACE_SMASH_AIR.location());

	@ModifyVariable(method = "play", at = @At("HEAD"), argsOnly = true)
	private SoundInstance leftarm$replaceMaceSound(SoundInstance sound) {
		if (!LeftArmConfig.get().maceCustomSound || !MACE_SOUNDS.contains(sound.getIdentifier())) {
			return sound;
		}
		// getVolume()/getPitch() здесь вызывать нельзя: звук ещё не загружен (resolve) и игра падает с NPE.
		// Булава в ванилле всё равно играет звук с громкостью и тоном 1.0.
		return new SimpleSoundInstance(MACE_HIT, sound.getSource(), 1.0f, 1.0f,
				RandomSource.create(), false, 0, sound.getAttenuation(),
				sound.getX(), sound.getY(), sound.getZ(), sound.isRelative());
	}
}
