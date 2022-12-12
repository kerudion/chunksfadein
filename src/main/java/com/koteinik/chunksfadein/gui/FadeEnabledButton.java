package com.koteinik.chunksfadein.gui;

import com.koteinik.chunksfadein.config.Config;

import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class FadeEnabledButton extends ButtonWidget {
    private static final int buttonW = 150;
    private static final int buttonH = 20;

    public FadeEnabledButton(GameOptionsScreen parent, int parentW, int parentH) {
        super(parentW / 2 - buttonW / 2, parentH / 2 - buttonH / 2 - 28 * 2,
                buttonW, buttonH, createText(),
                new PressAction() {
                    @Override
                    public void onPress(ButtonWidget button) {
                        Config.setBoolean(Config.FADE_ENABLED_KEY, !Config.isFadeEnabled);
                        button.setMessage(createText());
                    }
                }, DEFAULT_NARRATION_SUPPLIER);
    }

    private static Text createText() {
        Boolean isFadeEnabled = Config.isFadeEnabled;

        String color = isFadeEnabled ? "§2" : "§c";
        String enabledText = isFadeEnabled ? "YES" : "NO";

        return Text.of("Fade enabled: " + color + enabledText);
    }
}