package com.koteinik.chunksfadein;

import net.fabricmc.api.ModInitializer;

public class ChunksFadeIn implements ModInitializer {
    @Override
    public void onInitialize() {
        Config.loadConfigFile();
        Config.loadConfig();
    }
}
