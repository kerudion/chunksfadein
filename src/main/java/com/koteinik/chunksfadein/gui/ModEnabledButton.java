package com.koteinik.chunksfadein.gui;

import com.koteinik.chunksfadein.config.Config;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class ModEnabledButton extends ButtonWidget {
    private static final int buttonW = 150;
    private static final int buttonH = 20;

    public ModEnabledButton(SettingsScreen parent, int parentW, int parentH) {
        super(parentW / 2 - buttonW - 4, parentH / 2 - buttonH / 2 - 28 * 4,
                buttonW, buttonH, createText(),
                new PressAction() {
                    @Override
                    public void onPress(ButtonWidget button) {
                        Config.setBoolean(Config.MOD_ENABLED_KEY, !Config.isModEnabled);
                        button.setMessage(createText());
                    }
                }, DEFAULT_NARRATION_SUPPLIER);
        if (!needToDisable())
            this.setTooltip(Tooltip.of(Text.of("This option can't be changed in-game")));
        this.active = needToDisable();
    }

    private static Text createText() {
        Boolean isModEnabled = Config.isModEnabled;

        String color = isModEnabled ? "§2" : "§c";
        String enabledText = isModEnabled ? "YES" : "NO";

        return Text.of("Mod enabled: " + color + enabledText);
    }

    private static boolean needToDisable() {
        return MinecraftClient.getInstance().getServer() == null;
    }
}