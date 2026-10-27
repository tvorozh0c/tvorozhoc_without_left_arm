package com.tvorozhoc.leftarm.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.tvorozhoc.leftarm.LeftArmConfig;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Событие 35 — срабатывание тотема: здесь игра проигрывает звук и анимацию. */
@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
	@WrapOperation(method = "handleEntityEvent", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/multiplayer/ClientLevel;playLocalSound(DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FFZ)V"))
	private void leftarm$totemSound(ClientLevel level, double x, double y, double z, SoundEvent sound, SoundSource source,
									float volume, float pitch, boolean distanceDelay, Operation<Void> original) {
		if (LeftArmConfig.get().totemSound) {
			original.call(level, x, y, z, sound, source, volume, pitch, distanceDelay);
		}
	}

	@WrapOperation(method = "handleEntityEvent", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/GameRenderer;displayItemActivation(Lnet/minecraft/world/item/ItemStack;)V"))
	private void leftarm$totemAnimation(GameRenderer renderer, ItemStack stack, Operation<Void> original) {
		if (LeftArmConfig.get().totemAnimation) {
			original.call(renderer, stack);
		}
	}
}
