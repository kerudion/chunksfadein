package com.koteinik.chunksfadein.compat.sodium.gui;

import com.koteinik.chunksfadein.ShaderUtils;
import com.koteinik.chunksfadein.config.Config;

import com.koteinik.chunksfadein.core.TranslatableEnum;

public class CFIOptionsStorage {
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

	public void flush() {
		if (needReload) {
			ShaderUtils.reloadWorldRenderer();
			needReload = false;
		}

		Config.save();
	}
}
