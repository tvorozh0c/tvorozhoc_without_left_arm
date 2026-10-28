package com.tvorozhoc.leftarm;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

import java.util.function.IntConsumer;

/** Всплывающая палитра: квадрат насыщенность/яркость и полоска оттенка справа. */
public class ColorPicker {
	private static final int PAD = 6;
	private static final int SQUARE = 100;
	private static final int HUE_WIDTH = 12;
	private static final int PREVIEW_HEIGHT = 14;
	public static final int WIDTH = PAD + SQUARE + PAD + HUE_WIDTH + PAD;
	public static final int HEIGHT = PAD + SQUARE + PAD + PREVIEW_HEIGHT + PAD;

	private enum Drag { NONE, SQUARE, HUE }

	private final int x;
	private final int y;
	private final IntConsumer onChange;
	private float hue;
	private float saturation;
	private float value;
	private Drag drag = Drag.NONE;

	public ColorPicker(int x, int y, int rgb, IntConsumer onChange) {
		this.x = x;
		this.y = y;
		this.onChange = onChange;
		setFromRgb(rgb);
	}

	private void setFromRgb(int rgb) {
		float r = (rgb >> 16 & 0xFF) / 255f;
		float g = (rgb >> 8 & 0xFF) / 255f;
		float b = (rgb & 0xFF) / 255f;
		float max = Math.max(r, Math.max(g, b));
		float delta = max - Math.min(r, Math.min(g, b));
		value = max;
		saturation = max == 0 ? 0 : delta / max;
		if (delta == 0) {
			hue = 0;
		} else if (max == r) {
			hue = ((g - b) / delta) / 6f;
		} else if (max == g) {
			hue = ((b - r) / delta + 2) / 6f;
		} else {
			hue = ((r - g) / delta + 4) / 6f;
		}
		if (hue < 0) hue += 1;
	}

	public int rgb() {
		return Mth.hsvToRgb(Math.min(hue, 0.999f), saturation, value) & 0xFFFFFF;
	}

	public boolean isInside(double mouseX, double mouseY) {
		return mouseX >= x && mouseX < x + WIDTH && mouseY >= y && mouseY < y + HEIGHT;
	}

	public boolean isDragging() {
		return drag != Drag.NONE;
	}

	/** true, если клик попал в палитру. */
	public boolean mouseClicked(double mouseX, double mouseY) {
		int top = y + PAD;
		if (mouseY >= top && mouseY < top + SQUARE) {
			if (mouseX >= x + PAD && mouseX < x + PAD + SQUARE) {
				drag = Drag.SQUARE;
			} else if (mouseX >= hueX() && mouseX < hueX() + HUE_WIDTH) {
				drag = Drag.HUE;
			}
		}
		mouseDragged(mouseX, mouseY);
		return isInside(mouseX, mouseY);
	}

	public void mouseDragged(double mouseX, double mouseY) {
		float fy = Mth.clamp((float) (mouseY - y - PAD) / (SQUARE - 1), 0, 1);
		if (drag == Drag.SQUARE) {
			saturation = Mth.clamp((float) (mouseX - x - PAD) / (SQUARE - 1), 0, 1);
			value = 1 - fy;
		} else if (drag == Drag.HUE) {
			hue = fy;
		} else {
			return;
		}
		onChange.accept(rgb());
	}

	public void mouseReleased() {
		drag = Drag.NONE;
	}

	private int hueX() {
		return x + PAD + SQUARE + PAD;
	}

	public void render(GuiGraphics graphics, Font font) {
		graphics.fill(x, y, x + WIDTH, y + HEIGHT, 0xF0101010);
		graphics.renderOutline(x, y, WIDTH, HEIGHT, 0xFFFFFFFF);

		// Квадрат: слева направо растёт насыщенность, сверху вниз падает яркость
		int sx = x + PAD;
		int sy = y + PAD;
		for (int i = 0; i < SQUARE; i++) {
			int top = 0xFF000000 | Mth.hsvToRgb(Math.min(hue, 0.999f), i / (float) (SQUARE - 1), 1f);
			graphics.fillGradient(sx + i, sy, sx + i + 1, sy + SQUARE, top, 0xFF000000);
		}
		int markerX = sx + Math.round(saturation * (SQUARE - 1));
		int markerY = sy + Math.round((1 - value) * (SQUARE - 1));
		graphics.renderOutline(markerX - 2, markerY - 2, 5, 5, value > 0.5f ? 0xFF000000 : 0xFFFFFFFF);

		// Полоска оттенка
		int hx = hueX();
		for (int i = 0; i < SQUARE; i++) {
			graphics.fill(hx, sy + i, hx + HUE_WIDTH, sy + i + 1, 0xFF000000 | Mth.hsvToRgb(i / (float) SQUARE, 1f, 1f));
		}
		int hueY = sy + Math.round(hue * (SQUARE - 1));
		graphics.renderOutline(hx - 1, hueY - 1, HUE_WIDTH + 2, 3, 0xFFFFFFFF);

		// Итоговый цвет и его код
		int rgb = rgb();
		int py = sy + SQUARE + PAD;
		graphics.fill(sx, py, x + WIDTH - PAD, py + PREVIEW_HEIGHT, 0xFF000000 | rgb);
		String hex = String.format("#%06X", rgb);
		float luminance = 0.299f * (rgb >> 16 & 0xFF) + 0.587f * (rgb >> 8 & 0xFF) + 0.114f * (rgb & 0xFF);
		graphics.drawString(font, hex, x + (WIDTH - font.width(hex)) / 2, py + 3,
				luminance > 140 ? 0xFF000000 : 0xFFFFFFFF, false);
	}
}
