package com.koteinik.chunksfadein.core;

import com.koteinik.chunksfadein.extenstions.RenderRegionExt;
import com.koteinik.chunksfadein.extenstions.RenderRegionManagerExt;

import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegion;

public class ChunkAppearedLink {
    public static RenderRegionManagerExt regionManager = null;

    public static ChunkData getChunkData(int x, int y, int z) {
        if (regionManager == null)
            return null;

        RenderRegion region = regionManager.getRenderRegion(x, y, z);

        if (region == null)
            return ChunkData.FULLY_FADED;

        RenderRegionExt ext = (RenderRegionExt) region;

        return ext.getChunkData(x, y, z);
    }
}
