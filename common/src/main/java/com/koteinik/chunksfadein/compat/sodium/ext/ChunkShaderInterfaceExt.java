package com.koteinik.chunksfadein.compat.sodium.ext;

import net.caffeinemc.mods.sodium.client.gl.buffer.GlMutableBuffer;

public interface ChunkShaderInterfaceExt {
	void bindUniforms(GlMutableBuffer fadeDataBuffer);
}
