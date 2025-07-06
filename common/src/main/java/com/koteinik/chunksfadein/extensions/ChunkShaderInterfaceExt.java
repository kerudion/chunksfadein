package com.koteinik.chunksfadein.extensions;

import net.caffeinemc.mods.sodium.client.gl.buffer.GlMutableBuffer;

public interface ChunkShaderInterfaceExt {
    public void bindUniforms(GlMutableBuffer fadeDataBuffer);
}
