package com.koteinik.chunksfadein.compat.sodium.ext;

public interface ChunkRenderListExt {
	ByteIteratorExt getSectionsWithGeometryIterator(boolean reverse);
	RenderRegionExt region();
}
