package com.koteinik.chunksfadein.gui;

import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.hooks.CompatibilityHook;

import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class ShowModButtonInSettingsButton extends ButtonWidget {
    private static final int buttonW = 150;
    private static final int buttonH = 20;

    public ShowModButtonInSettingsButton(int parentW, int parentH) {
        super(parentW / 2 - buttonW / 2, parentH / 2 - buttonH / 2 - 28 * 3,
                buttonW, buttonH, createText(),
                new PressAction() {
                    @Override
                    public void onPress(ButtonWidget button) {
                        Config.setBoolean(Config.SHOW_MOD_BUTTON_IN_SETTINGS_KEY, !Config.showModButtonInSettings);
                        button.setMessage(createText());
                    }
                }, DEFAULT_NARRATION_SUPPLIER);
        if (!needToDisable())
            this.setTooltip(Tooltip.of(Text.of("This is explicitly set to \"ON\", because ModMenu is not installed")));
        this.active = needToDisable();
    }

    private static Text createText() {
        Boolean showModButtonInSettings = Config.showModButtonInSettings;

        String color = showModButtonInSettings ? "§2" : "§c";
        String enabledText = showModButtonInSettings ? "ON" : "OFF";

        return Text.of("Mod button in settings: " + color + enabledText);
    }

    private static boolean needToDisable() {
        return CompatibilityHook.isModMenuLoaded;
    }
}