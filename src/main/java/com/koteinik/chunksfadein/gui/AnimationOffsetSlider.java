package com.koteinik.chunksfadein.gui;

import com.koteinik.chunksfadein.config.Config;

import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

public class AnimationOffsetSlider extends SliderWidget {
    private static final int sliderW = 150;
    private static final int sliderH = 20;

    public AnimationOffsetSlider(int parentW, int parentH) {
        super(parentW / 2 - sliderW / 2, parentH / 2 - sliderH / 2 + 28 * 3,
                sliderW, sliderH,
                Text.of(getText(Config.animationInitialOffset)), 10);

        super.value = Config.animationInitialOffset / Config.MAX_ANIMATION_OFFSET;
    }

    @Override
    protected void applyValue() {
        Config.setDouble(Config.ANIMATION_OFFSET_KEY, value * Config.MAX_ANIMATION_OFFSET);
        this.value = Config.animationInitialOffset / Config.MAX_ANIMATION_OFFSET;
    }

    @Override
    protected void updateMessage() {
        setMessage(Text.of(getText(value * Config.MAX_ANIMATION_OFFSET)));
    }

    public void setValue(double value) {
        this.value = value;
        updateMessage();
    }

    private static String getText(double value) {
        return String.format("Animation start (-y): %.2f", value);
    }
}
