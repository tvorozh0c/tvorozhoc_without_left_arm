package com.tvorozhoc.leftarm;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/** Настройки мода, хранятся в config/tvorozhoc_without_left_arm.json. */
public class LeftArmConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("tvorozhoc_without_left_arm.json");

	private static LeftArmConfig instance = new LeftArmConfig();

	/** Положение/размер/прозрачность одного предмета. */
	public static class ItemSettings {
		public float x = 0;
		public float y = 0;
		public float z = 0;
		public float scale = 1;
		/** 0..1, где 1 — полностью непрозрачный. */
		public float opacity = 1;

		public boolean isDefault() {
			return x == 0 && y == 0 && z == 0 && scale == 1 && opacity >= 1;
		}

		public void reset() {
			x = y = z = 0;
			scale = opacity = 1;
		}
	}

	public ItemSettings shield = new ItemSettings();
	public ItemSettings totem = new ItemSettings();
	public boolean totemAnimation = true;
	public boolean totemSound = true;

	// Вкладка «Остальное»
	public boolean totemParticlesCustom = false;
	public String totemParticlesColor = "#FF69B4";
	public boolean maceParticlesCustom = false;
	public String maceParticlesColor = "#FF69B4";
	public boolean maceCustomSound = false;
	public boolean pinkEars = false;

	public static LeftArmConfig get() {
		return instance;
	}

	/** "#RRGGBB" → 0xRRGGBB, или -1 если строка некорректна. */
	public static int parseColor(String text) {
		if (text == null) return -1;
		String hex = text.startsWith("#") ? text.substring(1) : text;
		if (hex.length() != 6) return -1;
		try {
			return Integer.parseInt(hex, 16);
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	/** Настройки для предмета или null, если это не щит и не тотем. */
	public @Nullable ItemSettings forStack(ItemStack stack) {
		if (stack.is(Items.SHIELD)) {
			return shield;
		}
		if (stack.is(Items.TOTEM_OF_UNDYING)) {
			return totem;
		}
		return null;
	}

	public static void load() {
		if (!Files.exists(PATH)) {
			save();
			return;
		}
		try (Reader reader = Files.newBufferedReader(PATH)) {
			LeftArmConfig loaded = GSON.fromJson(reader, LeftArmConfig.class);
			if (loaded != null) {
				if (loaded.shield == null) loaded.shield = new ItemSettings();
				if (loaded.totem == null) loaded.totem = new ItemSettings();
				instance = loaded;
			}
		} catch (Exception e) {
			LeftArmClient.LOGGER.error("Не удалось прочитать {}", PATH, e);
		}
	}

	public static void save() {
		try {
			Files.createDirectories(PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(PATH)) {
				GSON.toJson(instance, writer);
			}
		} catch (IOException e) {
			LeftArmClient.LOGGER.error("Не удалось сохранить {}", PATH, e);
		}
	}
}
