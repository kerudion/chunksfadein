package com.koteinik.chunksfadein.gui;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class ResetButton extends ButtonWidget {
    private static final int buttonW = 40;
    private static final int buttonH = 20;

    public ResetButton(int parentW, int parentH, int offsetX, int offsetY, Runnable resetCallback) {
        super(parentW / 2 - buttonW / 2 + offsetX, parentH / 2 - buttonH / 2 + offsetY,
                buttonW, buttonH, Text.of("Reset"), new PressAction() {
                    @Override
                    public void onPress(ButtonWidget button) {
                        resetCallback.run();
                    }
                }, DEFAULT_NARRATION_SUPPLIER);
    }
}
