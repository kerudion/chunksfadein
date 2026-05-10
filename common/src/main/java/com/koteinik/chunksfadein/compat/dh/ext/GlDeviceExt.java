package com.koteinik.chunksfadein.compat.dh.ext;

import com.mojang.blaze3d.pipeline.RenderPipeline;

public interface GlDeviceExt {
	void cfi_evictPipeline(RenderPipeline pipeline);
}
