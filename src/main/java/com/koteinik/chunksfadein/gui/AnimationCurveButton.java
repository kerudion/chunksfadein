package com.koteinik.chunksfadein.gui;

import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.Curves;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class AnimationCurveButton extends ButtonWidget {
    private static final int buttonW = 150;
    private static final int buttonH = 20;

    public AnimationCurveButton(int parentW, int parentH) {
        super(parentW / 2 - buttonW / 2 - buttonW / 2 - 4, parentH / 2 - buttonH / 2 + 28,
                buttonW, buttonH, createText(),
                new PressAction() {
                    @Override
                    public void onPress(ButtonWidget button) {
                        Integer current = Config.animationCurve.ordinal();
                        Integer next = current + 1;

                        if (next > Curves.values().length - 1)
                            next = 0;

                        Config.setInteger(Config.ANIMATION_CURVE_KEY, next);
                        button.setMessage(createText());
                    }
                }, DEFAULT_NARRATION_SUPPLIER);
    }

    private static Text createText() {
        Curves curve = Config.animationCurve;

        return Text.of("Curve: §e" + enumNameToString(curve));
    }

    private static String enumNameToString(Enum<?> value) {
        String str = value.name();
        str = str.toLowerCase();
        str = str.replaceAll("_", " ");
        str = str.substring(0, 1).toUpperCase() + str.substring(1);

        return str;
    }
}