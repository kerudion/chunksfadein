package com.koteinik.chunksfadein;

import com.koteinik.chunksfadein.compat.dh.ext.LodRendererExt;
import com.koteinik.chunksfadein.hooks.CompatibilityHook;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pipeline.transform.TransformPatcher;
import net.minecraft.client.Minecraft;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

public class ShaderUtils {
	public static LodRendererExt lodRenderer = null;

	private static Object irisTransformCache;
	private static Method clearCache;

	static {
		if (CompatibilityHook.isIrisLoaded) {
			try {
				Field cache = TransformPatcher.class.getDeclaredField("cache");
				cache.setAccessible(true);

				irisTransformCache = cache.get(null);

				clearCache = Map.class.getDeclaredMethod("clear");
			} catch (Exception e) {
				Logger.error("Failed to get Iris methods:", e);
			}
		} else {
			irisTransformCache = null;
			clearCache = null;
		}
	}

	public static boolean reloadOnEveryChange() {
		return CompatibilityHook.isIrisLoaded || CompatibilityHook.isDHRenderingEnabled();
	}

	public static void reloadWorldRenderer() {
		try {
			if (CompatibilityHook.isIrisLoaded)
				clearCache.invoke(irisTransformCache);

			if (CompatibilityHook.isIrisShaderPackInUse()) {
				Iris.reload();
			} else {
				Minecraft minecraft = Minecraft.getInstance();
				minecraft.levelRenderer.allChanged();
			}

			if (CompatibilityHook.isDHRenderingEnabled() && lodRenderer != null)
				lodRenderer.rebuildShaders();
		} catch (Exception e) {
			Logger.warn("Failed to reload renderers:", e);
		}
	}
}
