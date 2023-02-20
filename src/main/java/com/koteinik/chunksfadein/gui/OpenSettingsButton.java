package com.koteinik.chunksfadein.gui;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class OpenSettingsButton extends ButtonWidget {
    private static Identifier MOD_ICON = new Identifier("chunksfadein", "icon.jpg");

    private static final int buttonW = 20;
    private static final int buttonH = 20;

    public OpenSettingsButton(Screen parent, MinecraftClient client, int parentW, int parentH) {
        super(parentW / 2 - 155 - buttonW - 5, parentH / 6 + 72 - 6, buttonH, buttonW, Text.of(null),
                new PressAction() {
                    @Override
                    public void onPress(ButtonWidget button) {
                        client.setScreen(new SettingsScreen(parent));
                    }
                });
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.renderButton(matrices, mouseX, mouseY, delta);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, MOD_ICON);

        RenderSystem.enableDepthTest();
        drawTexture(matrices, x + 1, y + 1, 0, 0, width - 2, height - 2, width - 2, height - 2);
    }
}
