package com.koteinik.chunksfadein.gui;

import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

import com.koteinik.chunksfadein.MathUtils;
import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.gui.components.ChunksFadeInButton;
import com.koteinik.chunksfadein.gui.components.ChunksFadeInSlider;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class GuiUtils {
    public static final int SPACING_Y = 28;
    public static final int SPACING_X = 4;
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
        return new TranslatableText(key, args);
    }

    public static ChunksFadeInButton choiceButton(Screen screen, int column, int row, String textKey,
            String configKey) {
        return choiceButton(screen, column, row, textKey, configKey, null, null);
    }

    public static ChunksFadeInButton choiceButton(Screen screen, int column, int row, String textKey,
            String configKey, Boolean forcedValue, Text tooltip) {
        return button(screen, column, row,
                () -> choiceText(textKey, Config.getBoolean(configKey)),
                () -> Config.flipBoolean(configKey), forcedValue, tooltip);
    }

    public static ChunksFadeInButton toggledButton(Screen screen, int column, int row, String textKey,
            String configKey) {
        return toggledButton(screen, column, row, textKey, configKey, null, null);
    }

    public static ChunksFadeInButton toggledButton(Screen screen, int column, int row, String textKey,
            String configKey, Boolean forcedValue, Text tooltip) {
        return button(screen, column, row,
                () -> toggledText(textKey, Config.getBoolean(configKey)),
                () -> Config.flipBoolean(configKey), forcedValue, tooltip);
    }

    public static ChunksFadeInButton doneButton(Screen screen) {
        return new ChunksFadeInButton(
                calculateX(screen.width, 0),
                screen.height - BUTTON_H - 8,
                BUTTON_W, BUTTON_H,
                () -> ScreenTexts.DONE, () -> screen.close());
    }

    public static ChunksFadeInSlider slider(SettingsScreen screen, int column, int row, DoubleSupplier updateValue,
            DoubleSupplier displayValue,
            String textKey, String configKey, double scale) {
        ChunksFadeInSlider slider = new ChunksFadeInSlider(
                calculateX(screen.width, column),
                calculateY(screen.height, row),
                BUTTON_W, BUTTON_H,
                updateValue,
                (value) -> GuiUtils.doubleText(textKey, displayValue.getAsDouble()),
                (value) -> Config.setDouble(configKey, value), scale);
        return slider;
    }

    public static ChunksFadeInButton button(Screen screen, int column, int row,
            Supplier<Text> createText, Runnable onPressed) {
        return button(screen, column, row, createText, onPressed, null, null);
    }

    public static ChunksFadeInButton button(Screen screen, int column, int row,
            Supplier<Text> createText, Runnable onPressed, Boolean forcedValue, Text tooltip) {
        return new ChunksFadeInButton(
                calculateX(screen.width, column),
                calculateY(screen.height, row),
                BUTTON_W, BUTTON_H,
                createText,
                onPressed,
                forcedValue,
                tooltip,
                screen);
    }

    private static int calculateY(int screenSize, int row) {
        return screenSize / 2 - BUTTON_H / 2 + row * SPACING_Y;
    }

    private static int calculateX(int screenSize, int column) {
        int halfScreen = screenSize / 2;

        return column == 0
                ? halfScreen - BUTTON_W / 2
                : halfScreen + BUTTON_W * (column - (column < 0 ? 0 : 1)) + SPACING_X * column;
    }

    private static String color(boolean value) {
        return value ? "§2" : "§c";
    }
}
