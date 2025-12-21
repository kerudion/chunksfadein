package com.koteinik.chunksfadein.compat.sodium.ext;

public interface ShaderBindingContextExt {
	GlUniformBlockExt bindUniformBlock(String name);

	GlUniformFloat2vExt bindUniformFloat2v(String name);

	GlUniformIntExt bindUniformInt(String name);
}
