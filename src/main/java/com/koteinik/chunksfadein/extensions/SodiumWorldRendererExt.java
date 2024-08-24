package com.koteinik.chunksfadein.extensions;

import net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager;
import org.jetbrains.annotations.Nullable;

public interface SodiumWorldRendererExt {
    float[] getAnimationOffset(int x, int y, int z);
    @Nullable
    RenderSectionManager getRenderSectionManager();
}
