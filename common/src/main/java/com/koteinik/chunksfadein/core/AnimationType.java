package com.koteinik.chunksfadein.core;

import com.koteinik.chunksfadein.crowdin.Translations;
import com.koteinik.chunksfadein.gui.SettingsScreen;

import net.caffeinemc.mods.sodium.client.gui.options.TextProvider;
import net.minecraft.network.chat.Component;

public enum AnimationType implements TranslatableEnum, TextProvider {
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

	@Override
	public Component getLocalizedName() {
		return getTranslation();
	}
}
