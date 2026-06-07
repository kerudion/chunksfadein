package com.koteinik.chunksfadein.compat.sodium.ext;

import java.util.Iterator;

public interface RenderSectionManagerExt {
	float[] getAnimationOffset(int x, int y, int z);

	float getFadeCoeff(int x, int y, int z);

	Iterator<ChunkRenderListExt> renderLists();
}
