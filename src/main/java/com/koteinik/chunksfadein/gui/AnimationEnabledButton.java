package com.koteinik.chunksfadein.gui;

import com.koteinik.chunksfadein.config.Config;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class AnimationEnabledButton extends ButtonWidget {
    private static final int buttonW = 150;
    private static final int buttonH = 20;

    public AnimationEnabledButton(int parentW, int parentH) {
        super(parentW / 2 - buttonW / 2, parentH / 2 - buttonH / 2,
                buttonW, buttonH, createText(),
                new PressAction() {
                    @Override
                    public void onPress(ButtonWidget button) {
                        Config.setBoolean(Config.ANIMATION_ENABLED_KEY, !Config.isAnimationEnabled);
                        button.setMessage(createText());
                    }
                }, DEFAULT_NARRATION_SUPPLIER);
    }

    private static Text createText() {
        Boolean isAnimationEnabled = Config.isAnimationEnabled;

        String color = isAnimationEnabled ? "§2" : "§c";
        String enabledText = isAnimationEnabled ? "YES" : "NO";

        return Text.of("Animation enabled: " + color + enabledText);
    }
}