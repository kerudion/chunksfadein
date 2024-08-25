package com.koteinik.chunksfadein.gui.components;

import java.util.function.Supplier;

import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class CFIButton extends ButtonWidget {
    private final Supplier<Text> createText;

    public CFIButton(int x, int y, int width, int height, Supplier<Text> createText, Runnable onPress) {
        this(x, y, width, height, createText, onPress, null, null);
    }

    public CFIButton(int x, int y, int width, int height, Supplier<Text> createText, Runnable onPress,
        Boolean forcedValue, Text tooltip) {
        super(x, y, width, height, createText.get(), (btn) -> {
            onPress.run();
            ((CFIButton) btn).updateText();
        }, DEFAULT_NARRATION_SUPPLIER);
        this.active = forcedValue == null;
        this.createText = createText;

        if (tooltip != null)
            this.setTooltip(Tooltip.of(tooltip));
    }

    public void updateText() {
        setMessage(createText.get());
    }
}
