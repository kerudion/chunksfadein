package com.koteinik.chunksfadein.core;

import com.koteinik.chunksfadein.config.Config;

public final class ChunkData {
    public static float[] FULLY_FADED = new float[] { 0f, 0f, 0f, 1f };
    public static float[] INITIAL_FADE;

    public static void reload() {
        INITIAL_FADE = new float[] { 0f, -Config.animationInitialOffset, 0f, Config.isFadeEnabled ? 0f : 1f };
    }
}
