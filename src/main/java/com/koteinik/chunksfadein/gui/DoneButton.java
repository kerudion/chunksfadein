package com.koteinik.chunksfadein.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;

public class DoneButton extends ButtonWidget {
    private static final int buttonW = 150;
    private static final int buttonH = 20;

    public DoneButton(SettingsScreen parent, MinecraftClient client, int parentW, int parentH) {
        super(parentW / 2 - buttonW / 2, parentH - buttonH - 8,
                buttonW, buttonH, ScreenTexts.DONE, new PressAction() {
                    @Override
                    public void onPress(ButtonWidget button) {
                        parent.close();
                    }
                }, DEFAULT_NARRATION_SUPPLIER);
    }
}
