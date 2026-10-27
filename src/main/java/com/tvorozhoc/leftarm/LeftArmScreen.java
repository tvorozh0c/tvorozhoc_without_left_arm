package com.tvorozhoc.leftarm;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

/**
 * Экран настроек. Сверху — выбор щит/тотем, справа — ползунки.
 * Предмет можно двигать мышью прямо по экрану, колесо мыши меняет размер.
 */
public class LeftArmScreen extends Screen {
	private enum Tab { SHIELD, TOTEM, OTHER }

	private static final int PANEL_WIDTH = 170;
	/** Сколько блоков смещения на один пиксель перетаскивания. */
	private static final float DRAG_SPEED = 0.004f;

	private static Tab tab = Tab.SHIELD;
	private static @Nullable ItemStack shieldPreview;
	private static @Nullable ItemStack totemPreview;

	private final @Nullable Screen parent;
	private final List<ValueSlider> sliders = new ArrayList<>();
	/** Поля ввода цвета — рядом с каждым рисуем квадрат с этим цветом. */
	private final List<EditBox> swatches = new ArrayList<>();
	private boolean draggingItem;

	public LeftArmScreen(@Nullable Screen parent) {
		super(Component.translatable("tvorozhoc_without_left_arm.title"));
		this.parent = parent;
	}

	/** Пока экран открыт, в левой руке показывается выбранный предмет — чтобы видеть результат. */
	public static ItemStack previewStack(ItemStack actual) {
		if (!(Minecraft.getInstance().screen instanceof LeftArmScreen) || tab == Tab.OTHER) {
			return actual;
		}
		if (tab == Tab.SHIELD) {
			if (shieldPreview == null) shieldPreview = new ItemStack(Items.SHIELD);
			return actual.is(Items.SHIELD) ? actual : shieldPreview;
		}
		if (totemPreview == null) totemPreview = new ItemStack(Items.TOTEM_OF_UNDYING);
		return actual.is(Items.TOTEM_OF_UNDYING) ? actual : totemPreview;
	}

	private LeftArmConfig.ItemSettings current() {
		return tab == Tab.SHIELD ? LeftArmConfig.get().shield : LeftArmConfig.get().totem;
	}

	private int panelX() {
		return width - PANEL_WIDTH - 10;
	}

	@Override
	protected void init() {
		sliders.clear();

		swatches.clear();

		// Вкладки сверху
		addTab(Tab.SHIELD, "shield", width / 2 - 154);
		addTab(Tab.TOTEM, "totem", width / 2 - 50);
		addTab(Tab.OTHER, "other", width / 2 + 54);

		int x = panelX();
		int y = 40;
		if (tab == Tab.OTHER) {
			initOther(x, y);
			return;
		}

		LeftArmConfig.ItemSettings s = current();
		sliders.add(addRenderableWidget(new ValueSlider(x, y, "x", -1.5, 1.5, false, () -> s.x, v -> s.x = (float) v)));
		sliders.add(addRenderableWidget(new ValueSlider(x, y += 24, "y", -1.5, 1.5, false, () -> s.y, v -> s.y = (float) v)));
		sliders.add(addRenderableWidget(new ValueSlider(x, y += 24, "z", -1.5, 1.0, false, () -> s.z, v -> s.z = (float) v)));
		sliders.add(addRenderableWidget(new ValueSlider(x, y += 24, "scale", 0.1, 3.0, true, () -> s.scale, v -> s.scale = (float) v)));
		sliders.add(addRenderableWidget(new ValueSlider(x, y += 24, "opacity", 0.0, 1.0, true, () -> s.opacity, v -> s.opacity = (float) v)));

		if (tab == Tab.TOTEM) {
			LeftArmConfig config = LeftArmConfig.get();
			addRenderableWidget(Button.builder(toggleLabel("animation", config.totemAnimation), b -> {
				config.totemAnimation = !config.totemAnimation;
				b.setMessage(toggleLabel("animation", config.totemAnimation));
			}).bounds(x, y += 30, PANEL_WIDTH, 20).build());
			addRenderableWidget(Button.builder(toggleLabel("sound", config.totemSound), b -> {
				config.totemSound = !config.totemSound;
				b.setMessage(toggleLabel("sound", config.totemSound));
			}).bounds(x, y += 24, PANEL_WIDTH, 20).build());
		}

		addRenderableWidget(Button.builder(Component.translatable("tvorozhoc_without_left_arm.reset"), b -> {
			current().reset();
			syncSliders();
		}).bounds(x, y += 30, PANEL_WIDTH, 20).build());
		addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> onClose())
				.bounds(x, y + 24, PANEL_WIDTH, 20).build());
	}

	private void addTab(Tab target, String key, int x) {
		Button button = addRenderableWidget(Button.builder(Component.translatable("tvorozhoc_without_left_arm.tab." + key),
				b -> switchTab(target)).bounds(x, 8, 100, 20).build());
		button.active = tab != target;
	}

	/** Вкладка «Остальное»: цвета частиц, звук булавы, ушки. */
	private void initOther(int x, int y) {
		LeftArmConfig config = LeftArmConfig.get();

		addRenderableWidget(Button.builder(customLabel("totem_particles", config.totemParticlesCustom, "custom"), b -> {
			config.totemParticlesCustom = !config.totemParticlesCustom;
			b.setMessage(customLabel("totem_particles", config.totemParticlesCustom, "custom"));
		}).bounds(x, y, PANEL_WIDTH, 20).build());
		addColorBox(x, y += 22, config.totemParticlesColor, v -> config.totemParticlesColor = v);

		addRenderableWidget(Button.builder(customLabel("mace_particles", config.maceParticlesCustom, "custom"), b -> {
			config.maceParticlesCustom = !config.maceParticlesCustom;
			b.setMessage(customLabel("mace_particles", config.maceParticlesCustom, "custom"));
		}).bounds(x, y += 28, PANEL_WIDTH, 20).build());
		addColorBox(x, y += 22, config.maceParticlesColor, v -> config.maceParticlesColor = v);

		addRenderableWidget(Button.builder(customLabel("mace_sound", config.maceCustomSound, "custom_sound"), b -> {
			config.maceCustomSound = !config.maceCustomSound;
			b.setMessage(customLabel("mace_sound", config.maceCustomSound, "custom_sound"));
		}).bounds(x, y += 28, PANEL_WIDTH, 20).build());

		addRenderableWidget(Button.builder(toggleLabel("ears", config.pinkEars), b -> {
			config.pinkEars = !config.pinkEars;
			b.setMessage(toggleLabel("ears", config.pinkEars));
		}).bounds(x, y += 24, PANEL_WIDTH, 20).build());

		addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> onClose())
				.bounds(x, y + 30, PANEL_WIDTH, 20).build());
	}

	private void addColorBox(int x, int y, String value, Consumer<String> setter) {
		EditBox box = addRenderableWidget(new EditBox(font, x, y, PANEL_WIDTH - 26, 18,
				Component.translatable("tvorozhoc_without_left_arm.color_hint")));
		box.setMaxLength(7);
		box.setValue(value);
		box.setTooltip(Tooltip.create(Component.translatable("tvorozhoc_without_left_arm.color_hint")));
		box.setResponder(text -> {
			if (LeftArmConfig.parseColor(text) >= 0) {
				setter.accept(text.startsWith("#") ? text.toUpperCase() : "#" + text.toUpperCase());
			}
		});
		swatches.add(box);
	}

	private static Component customLabel(String key, boolean custom, String customKey) {
		return Component.translatable("tvorozhoc_without_left_arm." + key,
				Component.translatable("tvorozhoc_without_left_arm." + (custom ? customKey : "vanilla")));
	}

	private static Component toggleLabel(String key, boolean enabled) {
		return Component.translatable("tvorozhoc_without_left_arm." + key,
				Component.translatable(enabled ? "options.on" : "options.off"));
	}

	private void switchTab(Tab newTab) {
		tab = newTab;
		rebuildWidgets();
	}

	private void syncSliders() {
		for (ValueSlider slider : sliders) {
			slider.sync();
		}
	}

	// ---- Перетаскивание предмета мышью ----

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (super.mouseClicked(event, doubleClick)) {
			return true;
		}
		draggingItem = event.button() == 0 && tab != Tab.OTHER;
		return draggingItem;
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
		if (draggingItem) {
			LeftArmConfig.ItemSettings s = current();
			s.x = Mth.clamp(s.x + (float) dx * DRAG_SPEED, -1.5f, 1.5f);
			s.y = Mth.clamp(s.y - (float) dy * DRAG_SPEED, -1.5f, 1.5f);
			syncSliders();
			return true;
		}
		return super.mouseDragged(event, dx, dy);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		draggingItem = false;
		return super.mouseReleased(event);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (mouseX < panelX() && tab != Tab.OTHER) {
			LeftArmConfig.ItemSettings s = current();
			s.scale = Mth.clamp(s.scale + (float) scrollY * 0.05f, 0.1f, 3f);
			syncSliders();
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	// ---- Отрисовка ----

	@Override
	public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		// Без затемнения и размытия, чтобы было видно руку. Затемняем только панель справа.
		graphics.fill(panelX() - 6, 34, width - 4, height - 4, 0x90000000);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		super.render(graphics, mouseX, mouseY, delta);
		for (EditBox box : swatches) {
			int color = LeftArmConfig.parseColor(box.getValue());
			int sx = box.getX() + box.getWidth() + 4;
			graphics.fill(sx, box.getY(), sx + 22, box.getY() + 18, 0xFFFFFFFF);
			graphics.fill(sx + 1, box.getY() + 1, sx + 21, box.getY() + 17, color >= 0 ? 0xFF000000 | color : 0xFF555555);
		}
		if (tab != Tab.OTHER) {
			graphics.drawCenteredString(font, Component.translatable("tvorozhoc_without_left_arm.hint"),
					(panelX() - 6) / 2, height - 20, 0xFFFFFFFF);
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void onClose() {
		LeftArmConfig.save();
		minecraft.setScreen(parent);
	}

	private static class ValueSlider extends AbstractSliderButton {
		private final String key;
		private final double min;
		private final double max;
		private final boolean percent;
		private final DoubleSupplier getter;
		private final DoubleConsumer setter;

		ValueSlider(int x, int y, String key, double min, double max, boolean percent, DoubleSupplier getter, DoubleConsumer setter) {
			super(x, y, PANEL_WIDTH, 20, Component.empty(), 0);
			this.key = key;
			this.min = min;
			this.max = max;
			this.percent = percent;
			this.getter = getter;
			this.setter = setter;
			sync();
		}

		void sync() {
			value = Mth.clamp((getter.getAsDouble() - min) / (max - min), 0, 1);
			updateMessage();
		}

		private double realValue() {
			return min + value * (max - min);
		}

		@Override
		protected void updateMessage() {
			double v = realValue();
			String text = percent ? Math.round(v * 100) + "%" : String.format("%.2f", v);
			setMessage(Component.translatable("tvorozhoc_without_left_arm.slider." + key, text));
		}

		@Override
		protected void applyValue() {
			setter.accept(realValue());
		}
	}
}
