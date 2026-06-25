package com.koteinik.chunksfadein.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.Identifier;

public class OpenSettingsButton extends Button.Plain {
	private static Identifier MOD_ICON = Identifier.fromNamespaceAndPath("chunksfadein", "icon.png");

	private static final int buttonW = 20;
	private static final int buttonH = 20;

	public OpenSettingsButton(Screen parent, Minecraft client, int x, int y) {
		super(
			x, y, buttonH, buttonW, CommonComponents.EMPTY,
			(btn) -> client.setScreenAndShow(new SettingsScreen(parent)), DEFAULT_NARRATION
		);
	}

	//		context.blit(
	//			RenderPipelines.GUI_TEXTURED,
	//			MOD_ICON,
	//			getX() + 1,
	//			getY() + 1,
	//			0,
	//			0,
	//			0,
	//			width - 2,
	//			height - 2,
	//			width - 2,
	//			height - 2
	//		);
}
