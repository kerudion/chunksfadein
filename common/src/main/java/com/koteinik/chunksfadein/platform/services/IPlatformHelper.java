package com.koteinik.chunksfadein.platform.services;

import java.io.File;

import com.koteinik.chunksfadein.core.SemanticVersion;

import net.minecraft.client.KeyMapping;

public interface IPlatformHelper {
    boolean isForge();

    boolean isModLoaded(String modId);

    File getConfigDirectory();

    SemanticVersion getModVersion();

    SemanticVersion getMinecraftVersion();

    KeyMapping registerKeyBind(KeyMapping mapping);
}
