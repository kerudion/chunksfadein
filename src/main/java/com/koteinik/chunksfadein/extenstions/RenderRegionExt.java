package com.koteinik.chunksfadein.extenstions;

import java.util.List;

import com.koteinik.chunksfadein.core.ChunkData;

import me.jellysquid.mods.sodium.client.gl.device.CommandList;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;

public interface RenderRegionExt {
    public void updateChunksFade(List<RenderSection> chunks, ChunkShaderInterfaceExt shader, CommandList commandList);
    public ChunkData getChunkData(int x, int y, int z);
}
