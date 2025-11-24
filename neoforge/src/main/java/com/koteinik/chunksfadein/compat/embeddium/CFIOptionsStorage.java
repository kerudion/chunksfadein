package com.koteinik.chunksfadein.compat.embeddium;

import com.koteinik.chunksfadein.ShaderUtils;
import com.koteinik.chunksfadein.config.Config;
import org.embeddedt.embeddium.api.options.structure.OptionStorage;

public class CFIOptionsStorage implements OptionStorage<Config> {
	private boolean needReload = false;

	public void setIntegerDirty(String key, int value) {
		if (Config.getInteger(key) != value)
			needReload = true;

		Config.setInteger(key, value);
	}

	public void setDoubleDirty(String key, double value) {
		if (Config.getDouble(key) != value)
			needReload = true;

		Config.setDouble(key, value);
	}

	public void setBooleanDirty(String key, boolean value) {
		if (Config.getBoolean(key) != value)
			needReload = true;

		Config.setBoolean(key, value);
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
