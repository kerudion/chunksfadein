package com.koteinik.chunksfadein.gui;

import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.hooks.CompatibilityHook;

import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class ShowModButtonInSettingsButton extends ButtonWidget {
    private static final int buttonW = 150;
    private static final int buttonH = 20;

    public ShowModButtonInSettingsButton(GameOptionsScreen parent, int parentW, int parentH) {
        super(parentW / 2 - buttonW / 2, parentH / 2 - buttonH / 2 - 28 * 3,
                buttonW, buttonH, createText(),
                new PressAction() {
                    @Override
                    public void onPress(ButtonWidget button) {
                        Config.setBoolean(Config.SHOW_MOD_BUTTON_IN_SETTINGS_KEY, !Config.showModButtonInSettings);
                        button.setMessage(createText());
                    }
                },
                new TooltipSupplier() {
                    @Override
                    public void onTooltip(ButtonWidget button, MatrixStack matrices, int mouseX, int mouseY) {
                        if (!needToDisable())
                            parent.renderTooltip(matrices, Text.of("This is explicitly set to \"ON\", because ModMenu is not installed"), mouseX,
                                    mouseY);
                    }
                });
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