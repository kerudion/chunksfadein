package com.koteinik.chunksfadein.gui.components;

import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.TranslatableEnum;
import com.koteinik.chunksfadein.gui.GuiUtils;
import com.koteinik.chunksfadein.gui.SettingsScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class CFIButton extends Button {
	private final Supplier<Component> createText;

	public CFIButton(int x, int y, int width, int height, Supplier<Component> createText, Runnable onPress, boolean active, Component tooltip) {
		super(
			x, y, width, height, createText.get(), (btn) -> {
				onPress.run();
				((CFIButton) btn).updateText();
			}, DEFAULT_NARRATION
		);
		this.active = active;
		this.createText = createText;

		if (tooltip != null)
			this.setTooltip(Tooltip.create(tooltip));
	}

	public void updateText() {
		setMessage(createText.get());
	}

	public static class CFIButtonBuilder {
		private int x = 0;
		private int y = 0;
		private int width = GuiUtils.BUTTON_W;
		private int height = GuiUtils.BUTTON_H;
		private Supplier<Component> createText = () -> Component.empty();
		private List<Runnable> onPress = new ArrayList<>();
		private boolean active = true;
		private MutableComponent tooltip = null;

		public CFIButtonBuilder() {}

		public CFIButtonBuilder x(int x) {
			this.x = x;

			return this;
		}

		public CFIButtonBuilder y(int y) {
			this.y = y;

			return this;
		}

		public CFIButtonBuilder width(int width) {
			this.width = width;

			return this;
		}

		public CFIButtonBuilder height(int height) {
			this.height = height;

			return this;
		}

		public CFIButtonBuilder text(Component component) {
			return text(() -> component);
		}

		public CFIButtonBuilder text(Supplier<Component> createText) {
			this.createText = createText;

			return this;
		}

		public CFIButtonBuilder onPress(Runnable onPress) {
			this.onPress.add(onPress);

			return this;
		}

		public CFIButtonBuilder tooltip(MutableComponent tooltip) {
			if (this.tooltip == null)
				this.tooltip = tooltip;
			else
				this.tooltip.append("\n").append(tooltip);

			return this;
		}

		public CFIButtonBuilder tooltipOverride(MutableComponent tooltip) {
			this.tooltip = tooltip;

			return this;
		}

		public CFIButtonBuilder active(boolean active) {
			this.active = active;

			return this;
		}

		public CFIButtonBuilder applyIf(boolean value, Consumer<CFIButtonBuilder> action) {
			if (value)
				action.accept(this);

			return this;
		}

		public CFIButton build() {
			return new CFIButton(
				x, y, width, height,
				createText,
				() -> onPress.forEach(Runnable::run),
				active,
				tooltip
			);
		}

		public static <T extends Enum<T> & TranslatableEnum> CFIButtonBuilder cycle(String textKey, String configKey, Class<T> clazz) {
			T[] constants = clazz.getEnumConstants();

			return new CFIButtonBuilder()
				.text(() -> coloredFormatted(textKey, "§e", constants[Config.getInteger(configKey)].getTranslation()))
				.onPress(() -> {
					int next = Config.getInteger(configKey) + 1;

					if (next >= constants.length)
						next = 0;

					Config.setInteger(configKey, next);
				})
				.tooltip(GuiUtils.tooltip(textKey));
		}

		public static CFIButtonBuilder toggle(String textKey, String configKey) {
			return binaryToggle(textKey, configKey, SettingsScreen.ON, SettingsScreen.OFF);
		}

		public static CFIButtonBuilder choice(String textKey, String configKey) {
			return binaryToggle(textKey, configKey, SettingsScreen.YES, SettingsScreen.NO);
		}

		private static CFIButtonBuilder binaryToggle(String textKey, String configKey, Component on, Component off) {
			return new CFIButtonBuilder()
				.text(() -> {
					boolean value = Config.getBoolean(configKey);

					return coloredFormatted(textKey, color(value), value ? on : off);
				})
				.onPress(() -> Config.flipBoolean(configKey))
				.tooltip(GuiUtils.tooltip(textKey));
		}

		private static Component coloredFormatted(String key, String color, Component arg) {
			return GuiUtils.text(key, color + arg.getString());
		}

		private static String color(boolean value) {
			return value ? "§2" : "§c";
		}
	}
}
