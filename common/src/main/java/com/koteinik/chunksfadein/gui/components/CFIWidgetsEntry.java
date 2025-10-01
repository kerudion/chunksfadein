package com.koteinik.chunksfadein.gui.components;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList.Entry;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;

import java.util.List;

public class CFIWidgetsEntry extends Entry<CFIWidgetsEntry> {
	private static final int SPACING_X = 4;
	private static final int BUTTON_W = 180;

	private final List<AbstractWidget> widgets;
	private final Screen parent;
	private final int y;

	public CFIWidgetsEntry(List<AbstractWidget> widgets, Screen parent, int y) {
		this.widgets = widgets;
		this.parent = parent;
		this.y = y;
	}

	@Override
	public List<? extends GuiEventListener> children() {
		return widgets;
	}

	@Override
	public List<? extends NarratableEntry> narratables() {
		return widgets;
	}

	@Override
	public void renderContent(GuiGraphics context, int mouseX, int mouseY, boolean hovered, float tickDelta) {
		for (int i = 0; i < widgets.size(); i++) {
			AbstractWidget widget = widgets.get(i);

			int gridX = i;
			if (widgets.size() > 1 && i == 0)
				gridX = -1;

			widget.setPosition(calculateX(gridX), y);
			widget.render(context, mouseX, mouseY, tickDelta);
		}
	}

	private int calculateX(int column) {
		int halfScreen = parent.width / 2;

		return column == 0
			? halfScreen - BUTTON_W / 2
			: halfScreen + BUTTON_W * (column - (column < 0 ? 0 : 1)) + SPACING_X * column;
	}
}
