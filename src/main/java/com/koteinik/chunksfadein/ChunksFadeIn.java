package com.koteinik.chunksfadein;

import com.koteinik.chunksfadein.config.Config;

import net.fabricmc.api.ModInitializer;

public class ChunksFadeIn implements ModInitializer {
    @Override
    public void onInitialize() {
        Config.loadConfigFile();
        Config.load();
    }
}
