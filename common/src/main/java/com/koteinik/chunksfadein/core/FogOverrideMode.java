package com.koteinik.chunksfadein.core;

import com.koteinik.chunksfadein.crowdin.Translations;
import com.koteinik.chunksfadein.gui.SettingsScreen;
import net.caffeinemc.mods.sodium.client.gui.options.TextProvider;
import net.minecraft.network.chat.Component;

public enum FogOverrideMode implements TranslatableEnum, TextProvider {
	BOTH,
	CYLINDRICAL,
	SPHERICAL,
	NONE;

	public final Component translation;

	FogOverrideMode() {
		this.translation = Translations.translatable(SettingsScreen.FOG_OVERRIDE + "." + name().toLowerCase());
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
