package com.koteinik.chunksfadein.extensions;

import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;
import org.jetbrains.annotations.Nullable;

public interface SodiumWorldRendererExt {
    float[] getAnimationOffset(int x, int y, int z);
    @Nullable
    RenderSectionManager getRenderSectionManager();
}
