package com.koteinik.chunksfadein.hooks;

import com.seibel.distanthorizons.api.enums.rendering.EDhApiRendererMode;
import net.irisshaders.iris.api.v0.IrisApi;

public class CompatibilityHook {
	public static final boolean isModMenuLoaded = isModMenuLoaded();
	public static final boolean isIrisLoaded = isIrisLoaded();
	public static final boolean isDHLoaded = isDHLoaded();

	public static boolean isIrisShaderPackInUse() {
		if (!isIrisLoaded) return false;

		try {
			return IrisApi.getInstance().isShaderPackInUse();
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean isDHSSAOEnabled() {
		if (!isDHLoaded) return false;

		try {
			return !isIrisShaderPackInUse()
				&& com.seibel.distanthorizons.core.config.Config.Client.Advanced.Graphics.Ssao.enableSsao.get();
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean isDHRenderingEnabled() {
		if (!isDHLoaded) return false;

		try {
			return com.seibel.distanthorizons.core.config.Config.Client.Advanced.Debugging.rendererMode.get()
				!= EDhApiRendererMode.DISABLED;
		} catch (Exception e) {
			return false;
		}
	}

	private static boolean isIrisLoaded() {
		try {
			Class.forName("net.irisshaders.iris.api.v0.IrisApi");
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	private static boolean isModMenuLoaded() {
		try {
			Class.forName("com.terraformersmc.modmenu.api.ModMenuApi");
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	private static boolean isDHLoaded() {
		try {
			Class.forName("com.seibel.distanthorizons.api.DhApi");
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}
}
