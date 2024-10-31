package com.koteinik.chunksfadein.platform;

import java.util.ServiceLoader;

import com.koteinik.chunksfadein.platform.services.IPlatformHelper;

public class Services {
    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);

    private static <T> T load(Class<T> clazz) {
        return ServiceLoader.load(clazz)
            .findFirst()
            .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
    }
}