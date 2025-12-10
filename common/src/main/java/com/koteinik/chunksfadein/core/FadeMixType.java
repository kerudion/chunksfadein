package com.koteinik.chunksfadein.core;

import com.koteinik.chunksfadein.crowdin.Translations;
import com.koteinik.chunksfadein.gui.SettingsScreen;
import net.caffeinemc.mods.sodium.client.gui.options.TextProvider;
import net.minecraft.network.chat.Component;

@SuppressWarnings("unused")
public enum FadeMixType implements TranslatableEnum, TextProvider {
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

	@Override
	public Component getLocalizedName() {
		return getTranslation();
	}
}
