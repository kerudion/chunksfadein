package com.koteinik.chunksfadein.compat.dh.ext;

import com.mojang.blaze3d.buffers.GpuBuffer;

public interface BlazeLodBufferContainerExt {
	void cfi_upload();

	GpuBuffer cfi_getBuffer();
}
