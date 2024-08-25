package com.koteinik.chunksfadein.gui;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

import com.koteinik.chunksfadein.MathUtils;
import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.gui.components.CFIButton;
import com.koteinik.chunksfadein.gui.components.CFISlider;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public class GuiUtils {
    private static final int BUTTON_W = 180;
    private static final int BUTTON_H = 20;

    public static Text toggledText(String key, boolean value) {
        return coloredFormatted(key, color(value), value ? SettingsScreen.ON : SettingsScreen.OFF);
    }

    public static Text choiceText(String key, boolean value) {
        return coloredFormatted(key, color(value), value ? SettingsScreen.YES : SettingsScreen.NO);
    }

    public static Text doubleText(String key, double value) {
        return formattedText(key, MathUtils.round(value, 2));
    }

    public static Text coloredFormatted(String key, String color, Text arg) {
        return formattedText(key, color + arg.getString());
    }

    public static Text text(String key) {
        return formattedText(key);
    }

    public static Text formattedText(String key, Object... args) {
        return Text.translatable(key, args);
    }

    public static CFIButton choiceButton(String textKey, String configKey) {
        return choiceButton(textKey, configKey, null, null);
    }

    public static CFIButton choiceButton(String textKey,
        String configKey, Boolean forcedValue, Text tooltip) {
        return button(
            () -> choiceText(textKey, Config.getBoolean(configKey)),
            () -> Config.flipBoolean(configKey), forcedValue, tooltip);
    }

    public static CFIButton toggledButton(String textKey, String configKey) {
        return toggledButton(textKey, configKey, null, null);
    }

    public static CFIButton toggledButton(String textKey, String configKey, Boolean forcedValue, Text tooltip) {
        return button(
            () -> toggledText(textKey, Config.getBoolean(configKey)),
            () -> Config.flipBoolean(configKey), forcedValue, tooltip);
    }

    public static CFIButton doneButton(Screen screen) {
        return new CFIButton(
            screen.width / 2 - BUTTON_W / 2,
            screen.height - BUTTON_H - 8,
            BUTTON_W, BUTTON_H,
            () -> ScreenTexts.DONE, () -> screen.close());
    }

    public static CFISlider slider(DoubleSupplier updateValue, DoubleSupplier displayValue,
        String textKey, String configKey, double scale) {
        return slider(
            updateValue, displayValue, textKey,
            (value) -> Config.setDouble(configKey, value), scale);
    }

    public static CFISlider slider(DoubleSupplier updateValue, DoubleSupplier displayValue,
        String textKey, DoubleConsumer applyValue, double scale) {
        return new CFISlider(
            0,
            0,
            BUTTON_W, BUTTON_H,
            updateValue,
            (value) -> GuiUtils.doubleText(textKey, displayValue.getAsDouble()),
            applyValue, scale);
    }

    public static CFIButton button(Supplier<Text> createText, Runnable onPressed) {
        return button(createText, onPressed, null, null);
    }

    public static CFIButton button(Supplier<Text> createText, Runnable onPressed, Boolean forcedValue, Text tooltip) {
        return new CFIButton(
            0,
            0,
            BUTTON_W, BUTTON_H,
            createText,
            onPressed,
            forcedValue,
            tooltip);
    }

    private static String color(boolean value) {
        return value ? "§2" : "§c";
    }
}
