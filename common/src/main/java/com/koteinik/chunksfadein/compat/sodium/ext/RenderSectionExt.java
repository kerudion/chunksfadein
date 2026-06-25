package com.koteinik.chunksfadein.compat.sodium.ext;

import com.koteinik.chunksfadein.compat.sodium.ChunkFadeInController;

public interface RenderSectionExt {
	boolean hasRenderedBefore();

	void setRenderedBefore();

	void dhMarkRendered();

	long calculateAndGetDelta();

	float[] getAnimationOffset();

	float getFadeCoeff();

	void incrementFadeCoeff(long delta, int regionIndex, int sectionIndex, ChunkFadeInController controller);

	void incrementAnimationOffset(long delta, int regionIndex, int sectionIndex, ChunkFadeInController controller);
}
