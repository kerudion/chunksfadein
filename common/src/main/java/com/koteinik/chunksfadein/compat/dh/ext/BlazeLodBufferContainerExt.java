package com.koteinik.chunksfadein.compat.dh.ext;

import com.mojang.renderpearl.api.buffers.GpuBuffer;

public interface BlazeLodBufferContainerExt {
	void cfi_upload();

	GpuBuffer cfi_getBuffer();
}
