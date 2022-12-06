package com.koteinik.chunksfadein.gui;

import com.koteinik.chunksfadein.Config;

import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

public class FadeTimeSlider extends SliderWidget {
    private static final int sliderW = 150;
    private static final int sliderH = 20;

    public FadeTimeSlider(int parentW, int parentH) {
        super(parentW / 2 - sliderW / 2, parentH / 2 - sliderH / 2,
                sliderW, sliderH,
                Text.of(getText(Config.getSecondsFromFadeCoeff())), 10);

        super.value = Config.getSecondsFromFadeCoeff() / 3;
    }

    @Override
    protected void applyValue() {
        Config.setFadeCoeffFromSeconds(value * 3);
    }

    @Override
    protected void updateMessage() {
        setMessage(Text.of(getText(value * 3)));
    }

    private static String getText(double value) {
        return String.format("Fade time: %.2f sec", value);
    }
}
