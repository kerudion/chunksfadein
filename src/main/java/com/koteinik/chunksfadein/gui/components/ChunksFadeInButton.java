package com.koteinik.chunksfadein.gui.components;

import java.util.function.Supplier;

import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class ChunksFadeInButton extends ButtonWidget {
    final Supplier<Text> createText;

    public ChunksFadeInButton(int x, int y, int width, int height, Supplier<Text> createText, Runnable onPress) {
        this(x, y, width, height, createText, onPress, null, null);
    }

    public ChunksFadeInButton(int x, int y, int width, int height, Supplier<Text> createText, Runnable onPress,
            Boolean forsedValue, Text tooltip) {
        super(x, y, width, height, createText.get(), (btn) -> {
            onPress.run();
            ((ChunksFadeInButton) btn).updateText();
        }, DEFAULT_NARRATION_SUPPLIER);
        this.active = forsedValue == null;
        this.createText = createText;

        if (forsedValue != null && tooltip != null)
            this.setTooltip(Tooltip.of(tooltip));
    }

    public void updateText() {
        setMessage(createText.get());
    }
}
