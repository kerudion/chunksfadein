package com.koteinik.chunksfadein.hooks;

import java.lang.reflect.Method;

public class IrisApiHook {
    private static final boolean isIrisLoaded = isIrisLoaded();

    private static Class<?> irisApiClass = null;
    private static Object irisApiInstance = null;
    private static Method irisShaderEnabledMethod = null;

    static {
        if (isIrisLoaded)
            try {
                irisApiClass = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
                irisApiInstance = irisApiClass.getMethod("getInstance").invoke(null);

                irisShaderEnabledMethod = irisApiClass.getMethod("isShaderPackInUse");
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    public static boolean isShaderPackInUse() {
        if (!isIrisLoaded)
            return false;

        try {
            boolean isShaderPackInUse = (boolean) irisShaderEnabledMethod.invoke(irisApiInstance);
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
