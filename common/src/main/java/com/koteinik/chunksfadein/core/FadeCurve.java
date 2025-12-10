package com.koteinik.chunksfadein.core;

import com.koteinik.chunksfadein.crowdin.Translations;
import com.koteinik.chunksfadein.gui.SettingsScreen;
import net.caffeinemc.mods.sodium.client.gui.options.TextProvider;
import net.minecraft.network.chat.Component;

import java.util.function.Function;

@SuppressWarnings("unused")
public enum FadeCurve implements TranslatableEnum, TextProvider {
	LINEAR((f) -> f),
	QUINTIC((f) -> f * f * f * (f * (f * 6f - 15f) + 10f));

	public final Component translation;
	private final Function<Float, Float> calculate;

	FadeCurve(Function<Float, Float> calculate) {
		this.calculate = calculate;
		this.translation = Translations.translatable(SettingsScreen.FADE_CURVE + "." + name().toLowerCase());
	}

	public Float calculate(Float in) {
		return calculate.apply(in);
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
