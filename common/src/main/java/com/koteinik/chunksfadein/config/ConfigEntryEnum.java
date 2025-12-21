package com.koteinik.chunksfadein.config;

import com.koteinik.chunksfadein.Logger;
import com.koteinik.chunksfadein.core.TranslatableEnum;
import com.moandjiezana.toml.Toml;

import java.util.Arrays;

public class ConfigEntryEnum<T extends Enum<T> & TranslatableEnum> extends ConfigEntry<T> {
	private final Class<T> enumType;

	public ConfigEntryEnum(Class<T> enumType, T defaultValue, String description, String configKey) {
		super(defaultValue, configKey, description, Type.STRING);

		this.enumType = enumType;
	}

	public void load(Toml toml) {
		T tomlValue = null;

		try {
			String value = (String) type.get(toml, configKey);
			if (value != null)
				try {
					tomlValue = Enum.valueOf(enumType, value.toUpperCase());
				} catch (Exception e) {
					Logger.warn(
						"Unknown enum value '%s', expected one of: %s"
							.formatted(value, Arrays.toString(enumType.getEnumConstants()))
					);
				}
		} catch (ClassCastException e) {
			Integer value = (Integer) Type.INTEGER.get(toml, configKey);
			if (value != null)
				try {
					tomlValue = enumType.getEnumConstants()[value];
				} catch (Exception e1) {
					Logger.warn(
						"Unknown enum value '%s', expected one of: %s"
							.formatted(value, Arrays.toString(enumType.getEnumConstants()))
					);
				}
		}

		if (tomlValue == null)
			tomlValue = defaultValue;

		value = tomlValue;
		pollListeners();
	}

	@Override
	public String toString() {
		return "# %s\n#\n".formatted(description.replace("\n", "\n# ")) +
			"# Default: %s\n".formatted(defaultValue.name().toLowerCase()) +
			"# Possible: %s\n".formatted(Arrays.toString(enumType.getEnumConstants()).toLowerCase()) +
			"%s = %s\n".formatted(configKey, type.makeString(value).toLowerCase());
	}
}
