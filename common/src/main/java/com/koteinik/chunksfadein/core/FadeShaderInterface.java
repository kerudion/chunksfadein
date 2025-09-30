package com.koteinik.chunksfadein.core;

import net.caffeinemc.mods.sodium.client.gl.buffer.GlMutableBuffer;
import net.caffeinemc.mods.sodium.client.gl.shader.uniform.GlUniformBlock;
import net.caffeinemc.mods.sodium.client.gl.shader.uniform.GlUniformFloat2v;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ShaderBindingContext;

public class FadeShaderInterface {
	private GlUniformBlock uniformFadeDatas;
	private GlUniformFloat2v screenSize;

	public FadeShaderInterface(ShaderBindingContext context) {
		this.uniformFadeDatas = context.bindUniformBlockOptional("cfi_ubo_ChunkFadeDatas", 1);
		this.screenSize = context.bindUniformOptional("cfi_screenSize", GlUniformFloat2v::new);
	}

	public void bindUniforms(GlMutableBuffer fadeDataBuffer) {
		if (uniformFadeDatas != null)
			uniformFadeDatas.bindBuffer(fadeDataBuffer);

		if (screenSize != null)
			screenSize.set(Utils.mainTargetWidth(), Utils.mainTargetHeight());
	}
}
