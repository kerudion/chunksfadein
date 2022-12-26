package com.koteinik.chunksfadein.core;

public final class ChunkData {
    public static ChunkData FULLY_FADED = new ChunkData(0f, 0f, 0f, 1f);

    public final float x;
    public final float y;
    public final float z;
    public final float fadeCoeff;

    public ChunkData(float x, float y, float z, float fadeCoeff) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.fadeCoeff = fadeCoeff;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;

        if (obj == this)
            return true;

        if (!(obj instanceof ChunkData))
            return false;

        ChunkData other = (ChunkData) obj;

        if (other.x != x)
            return false;
        if (other.y != y)
            return false;
        if (other.z != z)
            return false;
        if (other.fadeCoeff != fadeCoeff)
            return false;

        return true;
    }
}
