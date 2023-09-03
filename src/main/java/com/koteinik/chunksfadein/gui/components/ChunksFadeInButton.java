package com.koteinik.chunksfadein.gui.components;

import java.util.function.Supplier;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class ChunksFadeInButton extends ButtonWidget {
    final Supplier<Text> createText;

    public ChunksFadeInButton(int x, int y, int width, int height, Supplier<Text> createText, Runnable onPress) {
        this(x, y, width, height, createText, onPress, null, null, null);
    }

    public ChunksFadeInButton(int x, int y, int width, int height, Supplier<Text> createText, Runnable onPress,
            Boolean forcedValue, Text tooltip, Screen screen) {
        super(x, y, width, height, createText.get(), (btn) -> {
            onPress.run();
            ((ChunksFadeInButton) btn).updateText();
        }, forcedValue != null && tooltip != null ? new TooltipSupplier() {
            @Override
            public void onTooltip(ButtonWidget button, MatrixStack matrices, int mouseX, int mouseY) {
                screen.renderTooltip(matrices, tooltip, mouseX,
                        mouseY);
            }
        } : ButtonWidget.EMPTY);
        this.active = forcedValue == null;
        this.createText = createText;
    }

    public void updateText() {
        setMessage(createText.get());
    }
}
