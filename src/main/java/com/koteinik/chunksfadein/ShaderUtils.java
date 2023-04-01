package com.koteinik.chunksfadein;

import com.koteinik.chunksfadein.hooks.CompatibilityHook;

import net.coderbot.iris.Iris;
import net.minecraft.client.MinecraftClient;

public class ShaderUtils {
    @SuppressWarnings("resource")
    public static void reloadWorldRenderer() {
        try {
            if (CompatibilityHook.isIrisLoaded && CompatibilityHook.isIrisShaderPackInUse())
                Iris.reload();
            else
                MinecraftClient.getInstance().worldRenderer.reload();
        } catch (Exception e) {
            Logger.warn("Failed to reload world renderer. Error: " + e.getLocalizedMessage());
        }
    }
}
