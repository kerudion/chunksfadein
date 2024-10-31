package com.koteinik.chunksfadein.core;

import java.util.function.Function;

import com.koteinik.chunksfadein.MathUtils;
import com.koteinik.chunksfadein.gui.GuiUtils;
import com.koteinik.chunksfadein.gui.SettingsScreen;

import net.minecraft.network.chat.Component;

public enum Curve implements TranslatableEnum {
    LINEAR((f) -> f),
    EASE_OUT((f) -> 1f - MathUtils.pow(1 - f, 3)),
    EASE_CIRCULAR((f) -> {
        final float f1 = 2 * f;

        return f < 0.5f
                ? (1 - MathUtils.sqrt(1f - MathUtils.pow(f1, 2))) / 2f
                : (MathUtils.sqrt(1f - MathUtils.pow(-f1 + 2, 2)) + 1) / 2f;
    }),
    BOUNCE((f) -> {
        if (f < 1 / CurveConstants.d1) {
            return CurveConstants.n1 * f * f;
        } else if (f < 2 / CurveConstants.d1) {
            return CurveConstants.n1 * (f -= 1.5f / CurveConstants.d1) * f + 0.75f;
        } else if (f < 2.5 / CurveConstants.d1) {
            return CurveConstants.n1 * (f -= 2.25f / CurveConstants.d1) * f + 0.9375f;
        } else {
            return CurveConstants.n1 * (f -= 2.625f / CurveConstants.d1) * f + 0.984375f;
        }
    });

    public final Component translation;
    private final Function<Float, Float> calculate;

    private Curve(Function<Float, Float> calculate) {
        this.calculate = calculate;
        this.translation = GuiUtils.text(SettingsScreen.ANIMATION_CURVE + "." + name().toLowerCase());
    }

    public Float calculate(Float in) {
        return calculate.apply(in);
    }

    @Override
    public Component getTranslation() {
        return translation;
    }
}

class CurveConstants {
    public static final float n1 = 7.5625f;
    public static final float d1 = 2.75f;
}