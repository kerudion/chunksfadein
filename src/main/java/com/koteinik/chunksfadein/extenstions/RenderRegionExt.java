package com.koteinik.chunksfadein.extenstions;

import java.util.List;

import me.jellysquid.mods.sodium.client.gl.device.CommandList;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;

public interface RenderRegionExt {
    public void updateChunksFade(List<RenderSection> chunks, ChunkShaderInterfaceExt shader, CommandList commandList);
    public float[] getChunkData(int x, int y, int z);
    public void completeChunkFade(int x, int y, int z, boolean completeFade);
    public RenderSection getSection(int x, int y, int z);
}
