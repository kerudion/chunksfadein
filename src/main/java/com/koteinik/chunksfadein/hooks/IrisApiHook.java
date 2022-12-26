package com.koteinik.chunksfadein.hooks;

import com.koteinik.chunksfadein.extenstions.ChunkShaderInterfaceExt;

import net.irisshaders.iris.api.v0.IrisApi;

public class IrisApiHook {
    public static ChunkShaderInterfaceExt irisExt;
    public static final boolean isIrisLoaded = isIrisLoaded();

    public static boolean isShaderPackInUse() {
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
}
