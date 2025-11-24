package com.koteinik.chunksfadein.compat.embeddium.mixin;

import com.koteinik.chunksfadein.compat.sodium.ext.GlUniformIntExt;
import org.embeddedt.embeddium.impl.gl.shader.uniform.GlUniformInt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = GlUniformInt.class, remap = false)
public abstract class GlUniformIntMixin implements GlUniformIntExt {
	@Shadow
	public abstract void setInt(int value);

	@Override
	public void set(int value) {
		setInt(value);
	}
}
