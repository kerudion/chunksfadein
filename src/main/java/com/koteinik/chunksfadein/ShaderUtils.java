package com.koteinik.chunksfadein;

import com.koteinik.chunksfadein.hooks.IrisApiHook;

import net.coderbot.iris.Iris;
import net.minecraft.client.MinecraftClient;

public class ShaderUtils {
    @SuppressWarnings("resource")
    public static void reloadWorldRenderer() {
        try {
            if (IrisApiHook.isIrisLoaded && IrisApiHook.isShaderPackInUse())
                Iris.reload();
            else
                MinecraftClient.getInstance().worldRenderer.reload();
        } catch (Exception e) {
            Logger.warn("Failed to reload world renderer. Error: " + e.getLocalizedMessage());
        }
    }
}
