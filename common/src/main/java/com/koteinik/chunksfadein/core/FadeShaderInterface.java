package com.koteinik.chunksfadein.core;

import com.koteinik.chunksfadein.compat.sodium.ext.GlMutableBufferExt;
import com.koteinik.chunksfadein.compat.sodium.ext.GlUniformBlockExt;
import com.koteinik.chunksfadein.compat.sodium.ext.GlUniformFloat2vExt;
import com.koteinik.chunksfadein.compat.sodium.ext.ShaderBindingContextExt;

public class FadeShaderInterface {
	private GlUniformBlockExt uniformFadeDatas;
	private GlUniformFloat2vExt screenSize;

	public FadeShaderInterface(ShaderBindingContextExt context) {
		this.uniformFadeDatas = context.bindUniformBlock("cfi_ubo_ChunkFadeDatas");
		this.screenSize = context.bindUniformFloat2v("cfi_screenSize");
	}

	public void bindUniforms(GlMutableBufferExt fadeDataBuffer) {
		if (uniformFadeDatas != null)
			uniformFadeDatas.bindBuffer(fadeDataBuffer);

		if (screenSize != null)
			screenSize.set(Utils.mainTargetWidth(), Utils.mainTargetHeight());
	}
}
