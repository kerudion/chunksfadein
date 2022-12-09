package com.koteinik.chunksfadein.core;

import java.util.function.Function;

import com.koteinik.chunksfadein.MathUtils;

public enum Curves {
    LINEAR((f) -> f),
    EASE_OUT((f) -> 1f - MathUtils.pow(1 - f, 3));

    private final Function<Float, Float> calculate;

    private Curves(Function<Float, Float> calculate) {
        this.calculate = calculate;
    }

    public Float calculate(Float in) {
        return calculate.apply(in);
    }
}
