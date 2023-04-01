package com.koteinik.chunksfadein.hooks;

import net.irisshaders.iris.api.v0.IrisApi;

public class CompatibilityHook {
    public static final boolean isModMenuLoaded = isModMenuLoaded();
    public static final boolean isIrisLoaded = isIrisLoaded();

    public static boolean isIrisShaderPackInUse() {
        if (!isIrisLoaded)
            return false;

        try {
            boolean isShaderPackInUse = IrisApi.getInstance().isShaderPackInUse();
            return isShaderPackInUse;
        } catch (Exception e) {
            e.printStackTrace();
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
}
