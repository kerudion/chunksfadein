package com.koteinik.chunksfadein;

import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.ModrinthApi;

import de.guntram.mcmod.crowdintranslate.CrowdinTranslate;
import net.fabricmc.api.ClientModInitializer;

public class ChunksFadeIn implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Config.load();
        ModrinthApi.load();
        CrowdinTranslate.downloadTranslations("chunks-fade-in", "chunksfadein");
    }
}
