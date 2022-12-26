package com.koteinik.chunksfadein.gui;

import com.koteinik.chunksfadein.config.Config;

import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

public class AnimationTimeSlider extends SliderWidget {
    private static final int sliderW = 150;
    private static final int sliderH = 20;

    public AnimationTimeSlider(int parentW, int parentH) {
        super(parentW / 2 - sliderW / 2 + sliderW / 2+4, parentH / 2 - sliderH / 2 + 28 * 2,
                sliderW, sliderH,
                Text.of(getText(Config.secondsFromAnimationChange())), 10);

        super.value = Config.secondsFromAnimationChange() / Config.MAX_ANIMATION_TIME;
    }

    @Override
    protected void applyValue() {
        Config.setDouble(Config.ANIMATION_TIME_KEY, value * Config.MAX_ANIMATION_TIME);
        this.value = Config.secondsFromAnimationChange() / Config.MAX_ANIMATION_TIME;
    }

    @Override
    protected void updateMessage() {
        setMessage(Text.of(getText(value * Config.MAX_ANIMATION_TIME)));
    }

    public void setValue(double value) {
        this.value = value;
        updateMessage();
    }

    private static String getText(double value) {
        return String.format("Animation time: %.2f sec", value);
    }
}
