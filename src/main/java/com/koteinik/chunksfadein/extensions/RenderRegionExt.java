package com.koteinik.chunksfadein.extensions;

import net.caffeinemc.mods.sodium.client.gl.device.CommandList;

public interface RenderRegionExt {
    public void processChunk(RenderSectionExt chunk, int chunkId, int x, int y, int z);
    public void uploadToBuffer(ChunkShaderInterfaceExt shader, CommandList commandList);
}
