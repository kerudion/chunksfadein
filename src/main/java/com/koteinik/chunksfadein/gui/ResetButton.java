package com.koteinik.chunksfadein.gui;

import com.koteinik.chunksfadein.Config;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class ResetButton extends ButtonWidget {
    private static final int buttonW = 40;
    private static final int buttonH = 20;

    public ResetButton(int parentW, int parentH, int offsetX, int offsetY, FadeTimeSlider slider) {
        super(parentW / 2 - buttonW / 2 + offsetX, parentH / 2 - buttonH / 2 + offsetY,
                buttonW, buttonH, Text.of("Reset"), new PressAction() {
                    @Override
                    public void onPress(ButtonWidget button) {
                        Config.setFadeCoeffFromSeconds(Config.DEFAULT_FADE_TIME);
                        slider.setValue(Config.DEFAULT_FADE_TIME / Config.MAX_FADE_TIME);
                    }
                });
    }
}
