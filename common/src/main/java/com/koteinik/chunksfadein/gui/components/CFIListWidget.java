package com.koteinik.chunksfadein.gui.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;

import java.util.Arrays;

public class CFIListWidget extends ContainerObjectSelectionList<CFIWidgetsEntry> {
	private final Screen parent;
	private final int y;

	public CFIListWidget(Minecraft client, Screen parent, int width, int height, int y) {
		super(client, width, parent.height, y, height, 28);

		this.y = y;
		this.parent = parent;
	}

	public void add(AbstractWidget... widgets) {
		this.addEntry(new CFIWidgetsEntry(Arrays.asList(widgets), parent, y));
	}

	@Override
	protected int getScrollbarPosition() {
		return parent.width - 6;
	}

	@Override
	public int getRowWidth() {
		return parent.width - 32;
	}
}
