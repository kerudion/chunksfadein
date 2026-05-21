package com.koteinik.chunksfadein.compat.dh.ext;

public interface GlDhMetaRendererExt {
	DhRenderProgramExt getShader();

	void rebuildShaders();

	int activeColorTexture();
}
