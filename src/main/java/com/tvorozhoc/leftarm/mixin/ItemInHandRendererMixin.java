package com.tvorozhoc.leftarm.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.tvorozhoc.leftarm.LeftArmConfig;
import com.tvorozhoc.leftarm.LeftArmScreen;
import com.tvorozhoc.leftarm.TranslucentCollector;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {
	@Unique
	private InteractionHand leftarm$hand = InteractionHand.MAIN_HAND;

	/** Пока открыт экран настроек — показываем выбранный предмет в левой руке для предпросмотра. */
	@ModifyArg(method = "renderHandsWithItems", index = 5, at = @At(value = "INVOKE", ordinal = 1,
			target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderArmWithItem(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V"))
	private ItemStack leftarm$preview(ItemStack stack) {
		return LeftArmScreen.previewStack(stack);
	}

	@Inject(method = "renderArmWithItem", at = @At("HEAD"))
	private void leftarm$rememberHand(AbstractClientPlayer player, float partialTick, float pitch, InteractionHand hand,
									  float swing, ItemStack stack, float equip, PoseStack poseStack,
									  SubmitNodeCollector collector, int light, CallbackInfo ci) {
		leftarm$hand = hand;
	}

	@WrapOperation(method = "renderArmWithItem", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V"))
	private void leftarm$transform(ItemInHandRenderer self, LivingEntity entity, ItemStack stack, ItemDisplayContext context,
								   PoseStack poseStack, SubmitNodeCollector collector, int light, Operation<Void> original) {
		LeftArmConfig.ItemSettings settings = leftarm$hand == InteractionHand.OFF_HAND
				? LeftArmConfig.get().forStack(stack) : null;
		if (settings == null || settings.isDefault()) {
			original.call(self, entity, stack, context, poseStack, collector, light);
			return;
		}
		if (settings.opacity <= 0) {
			return;
		}

		poseStack.pushPose();
		poseStack.translate(settings.x, settings.y, settings.z);
		poseStack.scale(settings.scale, settings.scale, settings.scale);
		SubmitNodeCollector target = settings.opacity < 1 ? new TranslucentCollector(collector, settings.opacity) : collector;
		original.call(self, entity, stack, context, poseStack, target, light);
		poseStack.popPose();
	}
}
