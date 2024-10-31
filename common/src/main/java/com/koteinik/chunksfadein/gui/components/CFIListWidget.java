package com.koteinik.chunksfadein.gui.components;

import java.util.Arrays;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;

public class CFIListWidget extends ContainerObjectSelectionList<CFIWidgetsEntry> {
	private final Screen parent;

	public CFIListWidget(Minecraft client, Screen parent, int width, int height, int y) {
		super(client, width, height, y, 28);

		this.parent = parent;
	}

	public void add(AbstractWidget... widgets) {
		this.addEntry(new CFIWidgetsEntry(Arrays.asList(widgets), parent));
	}

	@Override
	public int getRowWidth() {
		return parent.width - 32;
	}
}
