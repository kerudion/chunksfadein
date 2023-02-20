package com.koteinik.chunksfadein.gui;

import com.koteinik.chunksfadein.config.Config;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class UpdateNotifierEnabledButton extends ButtonWidget {
    private static final int buttonW = 150;
    private static final int buttonH = 20;

    public UpdateNotifierEnabledButton(int parentW, int parentH) {
        super(parentW / 2 + 4, parentH / 2 - buttonH / 2 - 28 * 3,
                buttonW, buttonH, createText(),
                new PressAction() {
                    @Override
                    public void onPress(ButtonWidget button) {
                        Config.setBoolean(Config.UPDATE_NOTIFIER_ENABLED_KEY, !Config.isUpdateNotifierEnabled);
                        button.setMessage(createText());
                    }
                }, DEFAULT_NARRATION_SUPPLIER);
    }

    private static Text createText() {
        Boolean isUpdateNotifierEnabled = Config.isUpdateNotifierEnabled;

        String color = isUpdateNotifierEnabled ? "§2" : "§c";
        String enabledText = isUpdateNotifierEnabled ? "ON" : "OFF";

        return Text.of("Update notifier: " + color + enabledText);
    }
}