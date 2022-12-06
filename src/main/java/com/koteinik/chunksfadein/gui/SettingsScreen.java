package com.koteinik.chunksfadein.gui;

import com.koteinik.chunksfadein.Config;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class SettingsScreen extends GameOptionsScreen {
    private FadeTimeSlider slider;
    private DoneButton doneButton;

    @SuppressWarnings("resource")
    public SettingsScreen(Screen parent) {
        super(parent, MinecraftClient.getInstance().options, Text.of("Chunks fade in configuration"));
    }

    @Override
    public void init() {
        slider = new FadeTimeSlider(width, height);
        doneButton = new DoneButton(this, client, width, height);
        addDrawableChild(slider);
        addDrawableChild(doneButton);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void removed() {
        super.removed();
        Config.saveConfig();
    }
}
