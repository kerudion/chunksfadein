package com.koteinik.chunksfadein.compat.sodium.gui;

import com.koteinik.chunksfadein.ShaderUtils;
import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.TranslatableEnum;
import me.jellysquid.mods.sodium.client.gui.options.storage.OptionStorage;

public class CFIOptionsStorage implements OptionStorage<Config> {
	private boolean needReload = false;

	public void setBooleanDirty(String key, boolean value) {
		if (Config.getBoolean(key) != value)
			needReload = true;

		Config.setBoolean(key, value);
	}

	public void setIntegerDirty(String key, int value) {
		if (Config.getInteger(key) != value)
			needReload = true;

		Config.setInteger(key, value);
	}

	public <T extends Enum<T> & TranslatableEnum> void setEnumDirty(String key, T value) {
		if (!Config.getEnum(key).equals(value))
			needReload = true;

		Config.setEnum(key, value);
	}

	public void setDoubleDirty(String key, double value) {
		if (Config.getDouble(key) != value)
			needReload = true;

		Config.setDouble(key, value);
	}

	public Config getData() {
		return new Config();
	}

	public void save() {
		if (needReload) {
			ShaderUtils.reloadWorldRenderer();
			needReload = false;
		}

		Config.save();
	}
}
