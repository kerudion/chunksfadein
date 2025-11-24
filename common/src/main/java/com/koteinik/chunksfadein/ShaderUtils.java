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
	private static Object monocleTransformCache;
	private static Method mapClear;

	static {
		if (CompatibilityHook.isIrisLoaded) {
			try {
				Field cache = TransformPatcher.class.getDeclaredField("cache");
				cache.setAccessible(true);

				irisTransformCache = cache.get(null);

				mapClear = Map.class.getDeclaredMethod("clear");
			} catch (Exception e) {
				Logger.error("Failed to get Iris methods:", e);
			}

			try {
				Field field = Class.forName("dev.ferriarnus.monocle.ShaderTransformer")
					.getDeclaredField("shaderTransformationCache");
				field.setAccessible(true);
				monocleTransformCache = field.get(null);
			} catch (Throwable t) {
				t.printStackTrace();
				monocleTransformCache = null;
			}
		} else {
			irisTransformCache = null;
			mapClear = null;
		}
	}

	public static boolean reloadOnEveryChange() {
		return CompatibilityHook.isIrisLoaded || CompatibilityHook.isDHRenderingEnabled();
	}

	public static void reloadWorldRenderer() {
		try {
			if (CompatibilityHook.isIrisLoaded) {
				mapClear.invoke(irisTransformCache);

				if (monocleTransformCache != null)
					mapClear.invoke(monocleTransformCache);
			}

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
