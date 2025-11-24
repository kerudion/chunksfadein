package com.koteinik.chunksfadein.compat.embeddium.mixin;

import com.koteinik.chunksfadein.compat.embeddium.GlUniformFloat2v;
import com.koteinik.chunksfadein.compat.sodium.ext.GlUniformBlockExt;
import com.koteinik.chunksfadein.compat.sodium.ext.GlUniformFloat2vExt;
import com.koteinik.chunksfadein.compat.sodium.ext.GlUniformIntExt;
import com.koteinik.chunksfadein.compat.sodium.ext.ShaderBindingContextExt;
import org.embeddedt.embeddium.impl.gl.GlObject;
import org.embeddedt.embeddium.impl.gl.shader.GlProgram;
import org.embeddedt.embeddium.impl.gl.shader.uniform.GlUniformBlock;
import org.embeddedt.embeddium.impl.gl.shader.uniform.GlUniformInt;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL32C;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = GlProgram.class, remap = false)
public abstract class ShaderBindingContextMixin extends GlObject implements ShaderBindingContextExt {
	@Override
	public GlUniformBlockExt bindUniformBlock(String name) {
		int index = GL32C.glGetUniformBlockIndex(handle(), name);
		if (index < 0)
			return null;

		GL32C.glUniformBlockBinding(this.handle(), index, 1);
		return (GlUniformBlockExt) new GlUniformBlock(1);
	}

	@Override
	public GlUniformFloat2vExt bindUniformFloat2v(String name) {
		int index = GL20C.glGetUniformLocation(handle(), name);

		return index < 0 ? null : new GlUniformFloat2v(index);
	}

	@Override
	public GlUniformIntExt bindUniformInt(String name) {
		int index = GL20C.glGetUniformLocation(handle(), name);

		return index < 0 ? null : (GlUniformIntExt) new GlUniformInt(index);
	}
}
