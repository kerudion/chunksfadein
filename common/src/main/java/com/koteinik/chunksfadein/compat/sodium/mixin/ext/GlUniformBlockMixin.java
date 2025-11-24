package com.koteinik.chunksfadein.compat.sodium.mixin.ext;

import com.koteinik.chunksfadein.compat.sodium.ext.GlMutableBufferExt;
import com.koteinik.chunksfadein.compat.sodium.ext.GlUniformBlockExt;
import net.caffeinemc.mods.sodium.client.gl.buffer.GlBuffer;
import net.caffeinemc.mods.sodium.client.gl.shader.uniform.GlUniformBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = GlUniformBlock.class, remap = false)
public abstract class GlUniformBlockMixin implements GlUniformBlockExt {
	@Shadow
	public abstract void bindBuffer(GlBuffer buffer);

	@Override
	public void bindBuffer(GlMutableBufferExt buffer) {
		bindBuffer((GlBuffer) buffer);
	}
}
