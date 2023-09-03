package com.koteinik.chunksfadein.core;

import com.koteinik.chunksfadein.gui.GuiUtils;
import com.koteinik.chunksfadein.gui.SettingsScreen;

import net.minecraft.text.Text;

public enum FadeTypes implements TranslatableEnum {
    FULL,
    LINED;

    public final Text translation;

    private FadeTypes() {
        this.translation = GuiUtils.text(SettingsScreen.FADE_TYPE + "." + name().toLowerCase());
    }

    @Override
    public Text getTranslation() {
        return translation;
    }
}
