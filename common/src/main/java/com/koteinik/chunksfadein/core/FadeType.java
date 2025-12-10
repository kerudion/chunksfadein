package com.koteinik.chunksfadein.core;

import com.koteinik.chunksfadein.crowdin.Translations;
import com.koteinik.chunksfadein.gui.SettingsScreen;
import net.caffeinemc.mods.sodium.client.gui.options.TextProvider;
import net.minecraft.network.chat.Component;

public enum FadeType implements TranslatableEnum, TextProvider {
	FULL,
	LINED,
	BLOCK,
	VERTEX,
	FRAGMENTED;

	public final Component translation;

	FadeType() {
		this.translation = Translations.translatable(SettingsScreen.FADE_TYPE + "." + name().toLowerCase());
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
