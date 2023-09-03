package com.koteinik.chunksfadein.core;

import java.util.List;

import com.koteinik.chunksfadein.extensions.ChunkShaderInterfaceExt;
import com.koteinik.chunksfadein.extensions.RenderRegionExt;
import com.koteinik.chunksfadein.extensions.RenderSectionExt;

import me.jellysquid.mods.sodium.client.gl.device.CommandList;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegion;

public class RegionChunkRendererBase {
    public static void updateFading(CommandList commandList, ChunkShaderInterfaceExt ext, RenderRegion region,
            List<RenderSection> chunks) {
        final RenderRegionExt regionExt = (RenderRegionExt) region;

        for (RenderSection section : chunks) {
            int chunkId = section.getChunkId();

            int x = section.getChunkX();
            int y = section.getChunkY();
            int z = section.getChunkZ();

            regionExt.processChunk((RenderSectionExt) section, chunkId, x, y, z);
        }

        regionExt.uploadToBuffer(ext, commandList);
    }
}
