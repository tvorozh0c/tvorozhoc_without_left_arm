package com.tvorozhoc.leftarm;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LeftArmClient implements ClientModInitializer {
	public static final String MOD_ID = "tvorozhoc_without_left_arm";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeClient() {
		LeftArmConfig.load();

		KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "main"));
		KeyMapping openSettings = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.tvorozhoc_without_left_arm.settings", InputConstants.Type.KEYSYM, InputConstants.KEY_O, category));

		LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, renderer, helper, context) -> {
			if (renderer instanceof AvatarRenderer<?> avatarRenderer) {
				helper.register(new PinkEarsLayer(avatarRenderer));
			}
		});

		ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			while (openSettings.consumeClick()) {
				mc.setScreen(new LeftArmScreen(mc.screen));
			}
		});
	}
}
