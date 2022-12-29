package com.koteinik.chunksfadein;

import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.ModrinthApi;

import net.fabricmc.api.ModInitializer;

public class ChunksFadeIn implements ModInitializer {
    @Override
    public void onInitialize() {
        Config.load();
        ModrinthApi.load();
    }
}
