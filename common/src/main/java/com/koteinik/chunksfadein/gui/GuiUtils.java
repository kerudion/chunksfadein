package com.koteinik.chunksfadein.gui;

import com.koteinik.chunksfadein.gui.components.CFIButton;
import com.koteinik.chunksfadein.gui.components.CFIButton.CFIButtonBuilder;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class GuiUtils {
	public static final int BUTTON_W = 180;
	public static final int BUTTON_H = 20;

	public static MutableComponent text(String key, String value) {
		return Component.translatable(key).append(": ").append(value);
	}

	public static MutableComponent tooltip(String key) {
		return Component.translatable(key + ".tooltip");
	}

	public static MutableComponent tooltip(String key, String custom) {
		return Component.translatable(key + "." + custom);
	}

	public static CFIButton doneButton(Screen screen) {
		return new CFIButtonBuilder()
			.x(screen.width / 2 - BUTTON_W / 2)
			.y(screen.height - BUTTON_H - 8)
			.text(CommonComponents.GUI_DONE)
			.onPress(screen::onClose)
			.build();
	}
}
