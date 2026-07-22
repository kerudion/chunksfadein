package com.koteinik.chunksfadein.compat.sodium.ext;

import net.caffeinemc.mods.sodium.client.gl.device.CommandList;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSection;

public interface RenderRegionExt {
	void processChunk(RenderSectionExt section, int sectionIndex);

	void uploadToBuffer(ChunkShaderInterfaceExt shader, CommandList commandList);

	RenderSection getSection(int sectionIndex);
}
