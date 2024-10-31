package com.koteinik.chunksfadein.extensions;

public interface RenderSectionExt {
    public boolean hasRenderedBefore();
    public void setRenderedBefore();
    public long calculateAndGetDelta();
    public float[] getAnimationOffset();
    public float getFadeCoeff();
    public float incrementFadeCoeff(long delta);
    public float[] incrementAnimationOffset(long delta);
}
