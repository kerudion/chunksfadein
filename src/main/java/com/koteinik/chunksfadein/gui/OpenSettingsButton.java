package com.koteinik.chunksfadein.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.util.Identifier;

public class OpenSettingsButton extends ButtonWidget {
    private static Identifier MOD_ICON = new Identifier("chunksfadein", "icon.jpg");

    private static final int buttonW = 20;
    private static final int buttonH = 20;

    public OpenSettingsButton(Screen parent, MinecraftClient client, int parentW, int parentH) {
        super(parentW / 2 - 155 - buttonW - 5, parentH / 6 + 72 - 6, buttonH, buttonW, ScreenTexts.EMPTY,
                new PressAction() {
                    @Override
                    public void onPress(ButtonWidget button) {
                        client.setScreen(new SettingsScreen(parent));
                    }
                }, DEFAULT_NARRATION_SUPPLIER);
    }

    @Override
    public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderButton(context, mouseX, mouseY, delta);
        drawTexture(context, MOD_ICON, getX() + 1, getY() + 1, 0, 0, 0, width - 2, height - 2, width - 2, height - 2);
    }
}
