package com.koteinik.chunksfadein.gui;

import com.koteinik.chunksfadein.ShaderUtils;
import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.FadeTypes;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class FadeTypeButton extends ButtonWidget {
    private static final int buttonW = 150;
    private static final int buttonH = 20;

    public FadeTypeButton(SettingsScreen parent, int parentW, int parentH) {
        super(parentW / 2 - buttonW / 2 - buttonW / 2 - 4, parentH / 2 - buttonH / 2 - 28,
                buttonW, buttonH, createText(),
                new PressAction() {
                    @Override
                    public void onPress(ButtonWidget button) {
                        Integer current = Config.fadeType.ordinal();
                        Integer next = current + 1;

                        if (next > FadeTypes.values().length - 1)
                            next = 0;

                        Config.setInteger(Config.FADE_TYPE_KEY, next);
                        button.setMessage(createText());

                        ShaderUtils.reloadWorldRenderer();
                    }
                });
    }

    private static Text createText() {
        FadeTypes curve = Config.fadeType;

        return Text.of("Fade type: §e" + enumNameToString(curve));
    }

    private static String enumNameToString(Enum<?> value) {
        String str = value.name();
        str = str.toLowerCase();
        str = str.replaceAll("_", " ");
        str = str.substring(0, 1).toUpperCase() + str.substring(1);

        return str;
    }
}