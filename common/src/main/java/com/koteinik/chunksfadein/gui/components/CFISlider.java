package com.koteinik.chunksfadein.gui.components;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.Function;

import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.gui.SettingsScreen;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

public class CFISlider extends AbstractSliderButton {
    private static final int RESET_BUTTON_W = 40;
    private static final int RESET_BUTTON_H = 20;

    final Function<Double, Component> getText;
    final DoubleConsumer applyValue;
    final DoubleSupplier updateValue;
    final double scale;

    public CFISlider(int x, int y, int width, int height, DoubleSupplier updateValue,
        Function<Double, Component> getText, DoubleConsumer applyValue, double scale) {
        super(x, y, width, height, getText.apply(updateValue.getAsDouble()), updateValue.getAsDouble());
        this.getText = getText;
        this.scale = scale;
        this.applyValue = applyValue;
        this.updateValue = updateValue;
    }

    @Override
    public void applyValue() {
        applyValue.accept(scaledValue());
    }

    @Override
    public void updateMessage() {
        setMessage(getText.apply(scaledValue()));
    }

    private double scaledValue() {
        return value * scale;
    }

    public CFIButton makeResetButton(String key) {
        return makeResetButton(() -> Config.reset(key));
    }

    public CFIButton makeResetButton(Runnable onPress) {
        return new CFIButton(0, 0, RESET_BUTTON_W, RESET_BUTTON_H,
            () -> SettingsScreen.RESET, () -> {
                onPress.run();
                updateMessage();
                value = updateValue.getAsDouble();
            });
    }
}
