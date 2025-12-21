package com.koteinik.chunksfadein.core;

import com.koteinik.chunksfadein.crowdin.Translations;
import com.koteinik.chunksfadein.gui.SettingsScreen;
import net.minecraft.network.chat.Component;

@SuppressWarnings("unused")
public enum FadeMixType implements TranslatableEnum {
	LINEAR,
	OKLAB;

	public final Component translation;

	FadeMixType() {
		this.translation = Translations.translatable(SettingsScreen.FADE_MIX_TYPE + "." + name().toLowerCase());
	}

	@Override
	public Component getTranslation() {
		return translation;
	}
}
