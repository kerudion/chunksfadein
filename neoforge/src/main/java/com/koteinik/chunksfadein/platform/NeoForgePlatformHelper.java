package com.koteinik.chunksfadein.platform;

import java.io.File;

import com.koteinik.chunksfadein.ChunksFadeIn;
import com.koteinik.chunksfadein.core.SemanticVersion;
import com.koteinik.chunksfadein.platform.services.IPlatformHelper;

import net.minecraft.client.KeyMapping;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;

public class NeoForgePlatformHelper implements IPlatformHelper {
    @Override
    public boolean isForge() {
        return true;
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public File getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get().toFile();
    }

    @Override
    public SemanticVersion getModVersion() {
        try {
            return new SemanticVersion(ModList.get().getModContainerById("chunksfadein").get().getModInfo().getVersion().toString(), false);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public SemanticVersion getMinecraftVersion() {
        try {
            return new SemanticVersion(ModList.get().getModContainerById("minecraft").get().getModInfo().getVersion().toString(), false);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public KeyMapping registerKeyBind(KeyMapping mapping) {
        ChunksFadeIn.KEYLIST.add(mapping);
        return mapping;
    }
}
