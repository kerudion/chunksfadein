package com.koteinik.chunksfadein.extensions;

import net.caffeinemc.mods.sodium.client.gl.device.CommandList;

public interface RenderRegionExt {
    public void processChunk(RenderSectionExt section, int sectionIndex);
    public void uploadToBuffer(ChunkShaderInterfaceExt shader, CommandList commandList);
}
