package com.koteinik.chunksfadein.compat.sodium.ext;

import com.koteinik.chunksfadein.core.DataBuffer;

public interface RenderSectionExt {
	boolean hasRenderedBefore();

	void setRenderedBefore();

	void dhMarkRendered();

	long calculateAndGetDelta();

	float[] getAnimationOffset();

	float getFadeCoeff();

	boolean incrementFadeCoeff(long delta, int sectionIndex, DataBuffer buffer);

	boolean incrementAnimationOffset(long delta, int sectionIndex, DataBuffer buffer);
}
