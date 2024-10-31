package com.koteinik.chunksfadein.gui;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

import com.koteinik.chunksfadein.MathUtils;
import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.gui.components.CFIButton;
import com.koteinik.chunksfadein.gui.components.CFISlider;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class GuiUtils {
    private static final int BUTTON_W = 180;
    private static final int BUTTON_H = 20;

    public static Component toggledText(String key, boolean value) {
        return coloredFormatted(key, color(value), value ? SettingsScreen.ON : SettingsScreen.OFF);
    }

    public static Component choiceText(String key, boolean value) {
        return coloredFormatted(key, color(value), value ? SettingsScreen.YES : SettingsScreen.NO);
    }

    public static Component doubleText(String key, double value) {
        return formattedText(key, MathUtils.round(value, 2));
    }

    public static Component coloredFormatted(String key, String color, Component arg) {
        return formattedText(key, color + arg.getString());
    }

    public static Component text(String key) {
        return formattedText(key);
    }

    public static Component formattedText(String key, Object... args) {
        return Component.translatable(key, args);
    }

    public static CFIButton choiceButton(String textKey, String configKey, Runnable onPressed) {
        return choiceButton(textKey, configKey, null, null, onPressed);
    }

    public static CFIButton choiceButton(String textKey, String configKey) {
        return choiceButton(textKey, configKey, null, null, null);
    }

    public static CFIButton choiceButton(String textKey,
        String configKey, Boolean forcedValue, Component tooltip, Runnable onPressed) {
        return button(
            () -> choiceText(textKey, Config.getBoolean(configKey)),
            () -> {
                Config.flipBoolean(configKey);

                if (onPressed != null)
                    onPressed.run();
            }, forcedValue, tooltip);
    }

    public static CFIButton toggledButton(String textKey, String configKey) {
        return toggledButton(textKey, configKey, null, null);
    }

    public static CFIButton toggledButton(String textKey, String configKey, Boolean forcedValue, Component tooltip) {
        return button(
            () -> toggledText(textKey, forcedValue == null ? Config.getBoolean(configKey) : forcedValue),
            () -> Config.flipBoolean(configKey), forcedValue, tooltip);
    }

    public static CFIButton doneButton(Screen screen) {
        return new CFIButton(
            screen.width / 2 - BUTTON_W / 2,
            screen.height - BUTTON_H - 8,
            BUTTON_W, BUTTON_H,
            () -> CommonComponents.GUI_DONE, () -> screen.onClose());
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

    public static CFIButton button(Supplier<Component> createText, Runnable onPressed) {
        return button(createText, onPressed, null, null);
    }

    public static CFIButton button(Supplier<Component> createText, Runnable onPressed, Boolean forcedValue, Component tooltip) {
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
