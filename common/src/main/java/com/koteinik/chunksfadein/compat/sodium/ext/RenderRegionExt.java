package com.koteinik.chunksfadein.compat.sodium.ext;

public interface RenderRegionExt {
	int REGION_SIZE = 256;

	void processChunk(RenderSectionExt section, int sectionIndex);

	void uploadToBuffer(ChunkShaderInterfaceExt shader, CommandListExt commandList);
}
