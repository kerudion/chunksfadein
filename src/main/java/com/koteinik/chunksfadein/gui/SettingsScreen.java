package com.koteinik.chunksfadein.gui;

import com.koteinik.chunksfadein.config.Config;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class SettingsScreen extends GameOptionsScreen {
    private FadeTimeSlider fadeSlider;
    private DoneButton doneButton;
    private ResetButton fadeResetButton;
    private ResetButton animationTimeResetButton;
    private ModEnabledButton modEnabledButton;
    private FadeEnabledButton fadeEnabledButton;
    private FadeTypeButton fadeTypeButton;
    private AnimationEnabledButton animationEnabledButton;
    private AnimationTimeSlider animationTimeSlider;
    private AnimationCurveButton animationCurveButton;
    private AnimationOffsetSlider animationOffsetSlider;
    private AnimateNearPlayerButton animateNearPlayerButton;
    private ResetButton animationOffsetResetButton;

    @SuppressWarnings("resource")
    public SettingsScreen(Screen parent) {
        super(parent, MinecraftClient.getInstance().options, Text.of("Chunks fade in configuration"));
    }

    @Override
    public void init() {
        modEnabledButton = new ModEnabledButton(this, width, height);
        fadeEnabledButton = new FadeEnabledButton(width, height);
        animationEnabledButton = new AnimationEnabledButton(width, height);

        fadeSlider = new FadeTimeSlider(width, height);
        fadeTypeButton = new FadeTypeButton(this, width, height);
        fadeResetButton = new ResetButton(width, height, 179, -28, () -> {
            Config.reset(Config.FADE_TIME_KEY);
            fadeSlider.setValue(Config.secondsFromFadeChange() / Config.MAX_FADE_TIME);
        });
        animationCurveButton = new AnimationCurveButton(width, height);
        animationTimeSlider = new AnimationTimeSlider(width, height);
        animationTimeResetButton = new ResetButton(width, height, 179, 28 * 2, () -> {
            Config.reset(Config.ANIMATION_TIME_KEY);
            animationTimeSlider.setValue(Config.secondsFromAnimationChange() / Config.MAX_ANIMATION_TIME);
        });
        animationOffsetSlider = new AnimationOffsetSlider(width, height);
        animationOffsetResetButton = new ResetButton(width, height, 179, 28, () -> {
            Config.reset(Config.ANIMATION_OFFSET_KEY);
            animationOffsetSlider.setValue(Config.animationInitialOffset / Config.MAX_ANIMATION_OFFSET);
        });
        animateNearPlayerButton = new AnimateNearPlayerButton(width, height);

        doneButton = new DoneButton(this, client, width, height);

        addDrawableChild(modEnabledButton);
        addDrawableChild(fadeEnabledButton);
        addDrawableChild(animationEnabledButton);

        addDrawableChild(fadeSlider);
        addDrawableChild(fadeTypeButton);
        addDrawableChild(fadeResetButton);
        addDrawableChild(animationCurveButton);
        addDrawableChild(animationTimeSlider);
        addDrawableChild(animationTimeResetButton);
        addDrawableChild(animationOffsetSlider);
        addDrawableChild(animationOffsetResetButton);
        addDrawableChild(animateNearPlayerButton);

        addDrawableChild(doneButton);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        TextRenderer textRenderer = minecraftClient.textRenderer;

        renderBackground(matrices);
        drawCenteredText(matrices, textRenderer, Text.of("Chunks fade in mod settings"), width / 2, height / 20,
                16777215 | 255 << 24);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void removed() {
        super.removed();
        Config.save();
    }
}
