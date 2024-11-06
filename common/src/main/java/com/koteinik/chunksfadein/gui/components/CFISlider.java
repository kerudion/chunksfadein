package com.koteinik.chunksfadein.gui.components;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.Function;

import com.koteinik.chunksfadein.MathUtils;
import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.gui.GuiUtils;
import com.koteinik.chunksfadein.gui.SettingsScreen;
import com.koteinik.chunksfadein.gui.components.CFIButton.CFIButtonBuilder;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetTooltipHolder;
import net.minecraft.network.chat.Component;

public class CFISlider extends AbstractSliderButton {
    private static final int RESET_BUTTON_W = 40;
    private static final int RESET_BUTTON_H = 20;

    private final Function<Double, Component> displayText;
    private final DoubleConsumer applyValue;
    private final DoubleSupplier getValue;
    private final WidgetTooltipHolder tooltip = new WidgetTooltipHolder();

    public CFISlider(int x, int y, int width, int height,
        DoubleSupplier getValue, DoubleConsumer applyValue, Function<Double, Component> displayText,
        Component tooltip) {
        super(x, y, width, height, displayText.apply(getValue.getAsDouble()), getValue.getAsDouble());
        this.displayText = displayText;
        this.applyValue = applyValue;
        this.getValue = getValue;

        if (tooltip != null)
            this.tooltip.set(Tooltip.create(tooltip));
    }

    @Override
    public void applyValue() {
        applyValue.accept(value);
    }

    @Override
    public void updateMessage() {
        setMessage(displayText.apply(getValue.getAsDouble()));
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        tooltip.refreshTooltipForNextRenderPass(isHovered, active, getRectangle());
    }

    public CFIButton makeResetButton(String key) {
        return new CFIButtonBuilder()
            .width(RESET_BUTTON_W)
            .height(RESET_BUTTON_H)
            .text(SettingsScreen.RESET)
            .onPress(() -> {
                Config.reset(key);

                updateMessage();
                value = getValue.getAsDouble();
            })
            .build();
    }

    public static class CFISliderBuilder {
        private int x = 0;
        private int y = 0;
        private int width = GuiUtils.BUTTON_W;
        private int height = GuiUtils.BUTTON_H;
        private DoubleConsumer applyValue = null;
        private DoubleSupplier getValue = null;
        private Function<Double, Component> displayText = null;
        private Component tooltip = null;

        public CFISliderBuilder() {}

        public CFISliderBuilder x(int x) {
            this.x = x;

            return this;
        }

        public CFISliderBuilder y(int y) {
            this.y = y;

            return this;
        }

        public CFISliderBuilder width(int width) {
            this.width = width;

            return this;
        }

        public CFISliderBuilder height(int height) {
            this.height = height;

            return this;
        }


        public CFISliderBuilder applyValue(DoubleConsumer applyValue) {
            this.applyValue = applyValue;

            return this;
        }

        public CFISliderBuilder getValue(DoubleSupplier getValue) {
            this.getValue = getValue;

            return this;
        }

        public CFISliderBuilder displayText(Function<Double, Component> displayText) {
            this.displayText = displayText;

            return this;
        }

        public CFISliderBuilder tooltip(String key) {
            return tooltip(Component.translatable(key));
        }

        public CFISliderBuilder tooltip(Component tooltip) {
            this.tooltip = tooltip;

            return this;
        }

        public CFISlider build() {
            return new CFISlider(x, y, width, height, getValue, applyValue, displayText, tooltip);
        }

        public static CFISliderBuilder range(String textKey, String configKey) {
            return range(textKey, configKey, Component.empty(), Config.getMin(configKey), Config.getMax(configKey), 0);
        }

        public static CFISliderBuilder range(String textKey, String configKey, int precision) {
            return range(textKey, configKey, Component.empty(), Config.getMin(configKey), Config.getMax(configKey), precision);
        }

        public static CFISliderBuilder range(String textKey, String configKey, Component units) {
            return range(textKey, configKey, units, Config.getMin(configKey), Config.getMax(configKey), 0);
        }

        public static CFISliderBuilder range(String textKey, String configKey, Component units, double min, double max, int precision) {
            return new CFISliderBuilder()
                .displayText(v -> GuiUtils.text(textKey, String.valueOf(MathUtils.round(MathUtils.lerp(min, max, v), precision))).append(units))
                .getValue(() -> MathUtils.rlerp(min, max, Config.getDouble(configKey)))
                .applyValue(v -> Config.setDouble(configKey, MathUtils.lerp(min, max, v)))
                .tooltip(GuiUtils.tooltip(textKey));
        }
    }
}
