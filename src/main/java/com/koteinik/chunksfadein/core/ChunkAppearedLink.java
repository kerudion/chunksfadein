package com.koteinik.chunksfadein.core;

import com.koteinik.chunksfadein.extenstions.RenderRegionExt;
import com.koteinik.chunksfadein.extenstions.RenderRegionManagerExt;

import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegion;

public class ChunkAppearedLink {
    public static RenderRegionManagerExt regionManager = null;

    public static RenderRegion getRenderRegion(int x, int y, int z) {
        if (regionManager == null)
            return null;

        RenderRegion region = regionManager.getRenderRegion(x, y, z);

        return region;
    }

    public static float[] getChunkData(int x, int y, int z) {
        RenderRegionExt region = (RenderRegionExt) getRenderRegion(x, y, z);
        if (region == null)
            return ChunkData.INITIAL_FADE;

        RenderSection section = region.getSection(x, y, z);

        if (section == null || !section.isBuilt())
            return ChunkData.INITIAL_FADE;

        float[] data = ((RenderRegionExt) region).getChunkData(x, y, z);
        return data;
    }

    public static void completeChunkFade(int x, int y, int z, boolean completeFade) {
        if (regionManager == null)
            return;

        RenderRegion region = regionManager.getRenderRegion(x, y, z);

        if (region == null)
            return;

        RenderRegionExt ext = (RenderRegionExt) region;

        ext.completeChunkFade(x, y, z, completeFade);
    }
}
