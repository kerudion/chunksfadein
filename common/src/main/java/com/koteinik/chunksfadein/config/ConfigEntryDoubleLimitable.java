package com.koteinik.chunksfadein.config;

import com.koteinik.chunksfadein.MathUtils;
import com.moandjiezana.toml.Toml;

public class ConfigEntryDoubleLimitable extends ConfigEntry<Double> {
	private final double max;
	private final double min;

	public ConfigEntryDoubleLimitable(double min, double max, double defaultValue, String description, String configKey) {
		super(defaultValue, configKey, description, Type.DOUBLE);
		this.max = max;
		this.min = min;
	}

	@Override
	public void load(Toml toml) {
		Double tomlValue = (Double) type.get(toml, configKey);

		if (tomlValue == null)
			tomlValue = defaultValue;

		value = tomlValue;
		clampValue();
		pollListeners();
	}

	@Override
	public Double set(Double value) {
		super.value = value;
		clampValue();
		pollListeners();

		return super.value;
	}

	public double getMin() {
		return min;
	}

	public double getMax() {
		return max;
	}

	@Override
	public String toString() {
		return "# %s\n#\n".formatted(description.replace("\n", "\n# ")) +
			"# Default: %s\n".formatted(defaultValue) +
			"# Range: [%s..%s]\n".formatted(min, max) +
			"%s = %s\n".formatted(configKey, type.makeString(value));
	}

	private void clampValue() {
		value = MathUtils.clamp(value, min, max);
	}
}
