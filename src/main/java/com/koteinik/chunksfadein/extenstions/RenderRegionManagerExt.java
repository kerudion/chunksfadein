package com.koteinik.chunksfadein.extenstions;

import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegion;

public interface RenderRegionManagerExt {
    public RenderRegion getRenderRegion(int chunkX, int chunkY, int chunkZ);
}
