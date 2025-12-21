package com.koteinik.chunksfadein.core;

import com.koteinik.chunksfadein.crowdin.Translations;
import com.koteinik.chunksfadein.gui.SettingsScreen;

import net.minecraft.network.chat.Component;

public enum AnimationType implements TranslatableEnum {
	FULL,
	SCALE,
	JAGGED,
	DISPLACEMENT;

	public final Component translation;

	AnimationType() {
		this.translation = Translations.translatable(SettingsScreen.ANIMATION_TYPE + "." + name().toLowerCase());
	}

	@Override
	public Component getTranslation() {
		return translation;
	}
}
