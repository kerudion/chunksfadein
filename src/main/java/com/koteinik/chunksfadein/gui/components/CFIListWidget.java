package com.koteinik.chunksfadein.gui.components;

import java.util.Arrays;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ElementListWidget;

public class CFIListWidget extends ElementListWidget<CFIWidgetsEntry> {
	private final Screen parent;

	public CFIListWidget(MinecraftClient client, Screen parent, int width, int height, int y) {
		super(client, width, height, y, 28);

		this.parent = parent;
	}

	public void add(ClickableWidget... widgets) {
		this.addEntry(new CFIWidgetsEntry(Arrays.asList(widgets), parent));
	}

	@Override
	public int getRowWidth() {
		return parent.width - 32;
	}
}
