package com.koteinik.chunksfadein.gui;

import com.koteinik.chunksfadein.Config;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class EnabledButton extends ButtonWidget {
    private static final int buttonW = 150;
    private static final int buttonH = 20;

    public EnabledButton(GameOptionsScreen parent, int parentW, int parentH) {
        super(parentW / 2 - buttonW / 2, parentH / 2 - buttonH / 2 - 28,
                buttonW, buttonH, createText(),
                new PressAction() {
                    @Override
                    public void onPress(ButtonWidget button) {
                        Config.isModEnabled = !Config.isModEnabled;
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
        String color = Config.isModEnabled ? "§2" : "§c";
        String enabledText = Config.isModEnabled ? "YES" : "NO";

        return Text.of("Enabled: " + color + enabledText);
    }

    private static boolean needToDisable() {
        return MinecraftClient.getInstance().getGame().getCurrentSession() == null;
    }
}