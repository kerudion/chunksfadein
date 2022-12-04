package com.koteinik.chunksfadein.extenstions;

import java.util.List;

import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;

public interface RenderRegionArenasExt {
    public void updateChunksFade(List<RenderSection> chunks, ChunkShaderInterfaceExt shader);
}
