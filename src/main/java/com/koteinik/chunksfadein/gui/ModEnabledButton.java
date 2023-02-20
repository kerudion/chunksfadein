package com.koteinik.chunksfadein.gui;

import com.koteinik.chunksfadein.config.Config;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class ModEnabledButton extends ButtonWidget {
    private static final int buttonW = 150;
    private static final int buttonH = 20;

    public ModEnabledButton(GameOptionsScreen parent, int parentW, int parentH) {
        super(parentW / 2 - buttonW - 4, parentH / 2 - buttonH / 2 - 28 * 3,
                buttonW, buttonH, createText(),
                new PressAction() {
                    @Override
                    public void onPress(ButtonWidget button) {
                        Config.setBoolean(Config.MOD_ENABLED_KEY, !Config.isModEnabled);
                        button.setMessage(createText());
                    }
                },
                new TooltipSupplier() {
                    @Override
                    public void onTooltip(ButtonWidget button, MatrixStack matrices, int mouseX, int mouseY) {
                        if (!needToDisable())
                            parent.renderTooltip(matrices, Text.of("This option can't be changed in-game"), mouseX,
                                    mouseY);
                    }
                });
        this.active = needToDisable();
    }

    private static Text createText() {
        Boolean isModEnabled = Config.isModEnabled;

        String color = isModEnabled ? "§2" : "§c";
        String enabledText = isModEnabled ? "YES" : "NO";

        return Text.of("Mod enabled: " + color + enabledText);
    }

    private static boolean needToDisable() {
        return MinecraftClient.getInstance().getGame().getCurrentSession() == null;
    }
}