package com.koteinik.chunksfadein.gui.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;

import java.util.Arrays;

public class CFIListWidget extends ContainerObjectSelectionList<CFIWidgetsEntry> {
	public static final int ITEM_HEIGHT = 28;

	private final Screen parent;
	private final int y;
	private int i = 0;

	public CFIListWidget(Minecraft client, Screen parent, int width, int height, int y) {
		super(client, width, height, y, ITEM_HEIGHT);

		this.y = y;
		this.parent = parent;
	}

	public void add(AbstractWidget... widgets) {
		this.addEntry(new CFIWidgetsEntry(Arrays.asList(widgets), parent, this, y + 4 + i * ITEM_HEIGHT));
		i++;
	}

	@Override
	public int getRowWidth() {
		return parent.width - 32;
	}
}
