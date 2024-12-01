package com.koteinik.chunksfadein.extensions;

import com.koteinik.chunksfadein.core.DataBuffer;

public interface RenderSectionExt {
    public boolean hasRenderedBefore();
    public void setRenderedBefore();
    public long calculateAndGetDelta();
    public float[] getAnimationOffset();
    public float getFadeCoeff();
    public boolean incrementFadeCoeff(long delta, int sectionIndex, DataBuffer buffer);
    public boolean incrementAnimationOffset(long delta, int sectionIndex, DataBuffer buffer);
}
