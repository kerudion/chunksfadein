package com.koteinik.chunksfadein.gui.components;

import java.util.function.Supplier;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

public class CFIButton extends Button {
    private final Supplier<Component> createText;

    public CFIButton(int x, int y, int width, int height, Supplier<Component> createText, Runnable onPress) {
        this(x, y, width, height, createText, onPress, null, null);
    }

    public CFIButton(int x, int y, int width, int height, Supplier<Component> createText, Runnable onPress,
        Boolean forcedValue, Component tooltip) {
        super(x, y, width, height, createText.get(), (btn) -> {
            onPress.run();
            ((CFIButton) btn).updateText();
        }, DEFAULT_NARRATION);
        this.active = forcedValue == null;
        this.createText = createText;

        if (tooltip != null)
            this.setTooltip(Tooltip.create(tooltip));
    }

    public void updateText() {
        setMessage(createText.get());
    }
}
