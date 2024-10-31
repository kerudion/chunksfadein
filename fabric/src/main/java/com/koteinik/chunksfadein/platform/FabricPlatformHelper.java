package com.koteinik.chunksfadein.platform;

import java.io.File;

import com.koteinik.chunksfadein.core.SemanticVersion;
import com.koteinik.chunksfadein.platform.services.IPlatformHelper;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;

public class FabricPlatformHelper implements IPlatformHelper {
    @Override
    public boolean isForge() {
        return false;
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public File getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir().toFile();
    }

    @Override
    public SemanticVersion getModVersion() {
        try {
            return new SemanticVersion(FabricLoader.getInstance().getModContainer("chunksfadein").get().getMetadata()
                .getVersion().getFriendlyString(), false);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public SemanticVersion getMinecraftVersion() {
        try {
            return new SemanticVersion(FabricLoader.getInstance().getModContainer("minecraft").get().getMetadata()
                .getVersion().getFriendlyString(), false);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public KeyMapping registerKeyBind(KeyMapping mapping) {
        return KeyBindingHelper.registerKeyBinding(mapping);
    }
}
