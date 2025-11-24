package com.koteinik.chunksfadein.core;

import com.koteinik.chunksfadein.gui.SettingsScreen;
import net.minecraft.network.chat.Component;

public enum FogOverrideMode implements TranslatableEnum {
	CYLINDRICAL,
	NONE;

	public final Component translation;

	private FogOverrideMode() {
		this.translation = Component.translatable(SettingsScreen.FOG_OVERRIDE + "." + name().toLowerCase());
	}

	@Override
	public Component getTranslation() {
		return translation;
	}
}
