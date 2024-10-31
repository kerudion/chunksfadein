package com.koteinik.chunksfadein;

import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.Keybinds;
import com.koteinik.chunksfadein.core.ModrinthApi;
import com.koteinik.chunksfadein.crowdin.Translations;

import net.fabricmc.api.ClientModInitializer;

public class ChunksFadeIn implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Config.load();
        ModrinthApi.load();
        Translations.download();
        Keybinds.initKeybinds();
    }
}
