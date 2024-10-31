package com.koteinik.chunksfadein.core;

import com.koteinik.chunksfadein.gui.GuiUtils;
import com.koteinik.chunksfadein.gui.SettingsScreen;

import net.minecraft.network.chat.Component;

public enum FadeType implements TranslatableEnum {
    FULL,
    LINED,
    BLOCK,
    VERTEX;

    public final Component translation;

    private FadeType() {
        this.translation = GuiUtils.text(SettingsScreen.FADE_TYPE + "." + name().toLowerCase());
    }

    @Override
    public Component getTranslation() {
        return translation;
    }
}
