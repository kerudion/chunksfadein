package com.koteinik.chunksfadein;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import com.koteinik.chunksfadein.hooks.CompatibilityHook;

import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pipeline.transform.TransformPatcher;
import net.minecraft.client.Minecraft;

public class ShaderUtils {
    private static Object irisTransformCache;
    private static Method clearCache;

    static {
        try {
            Field cache = TransformPatcher.class.getDeclaredField("cache");
            cache.setAccessible(true);

            irisTransformCache = cache.get(null);

            clearCache = Map.class.getDeclaredMethod("clear");
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        } catch (Exception e) {
            Logger.warn("Failed to reload world renderer");
            e.printStackTrace();
        }
    }
}
