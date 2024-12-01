package com.koteinik.chunksfadein.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;

public class OpenSettingsButton extends Button {
    private static ResourceLocation MOD_ICON = ResourceLocation.fromNamespaceAndPath("chunksfadein", "icon.png");

    private static final int buttonW = 20;
    private static final int buttonH = 20;

    public OpenSettingsButton(Screen parent, Minecraft client, int x, int y) {
        super(x, y, buttonH, buttonW, CommonComponents.EMPTY,
            (btn) -> client.setScreen(new SettingsScreen(parent)), DEFAULT_NARRATION);
    }

    @Override
    public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.renderWidget(context, mouseX, mouseY, delta);
        context.blit(loc -> RenderType.gui(), MOD_ICON, getX() + 1, getY() + 1, 0, 0, 0, width - 2, height - 2, width - 2, height - 2);
    }
}
