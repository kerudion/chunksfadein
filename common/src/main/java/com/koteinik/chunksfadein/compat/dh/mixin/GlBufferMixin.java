package com.koteinik.chunksfadein.compat.dh.mixin;

import com.koteinik.chunksfadein.compat.dh.ext.GlBufferExt;
import com.mojang.blaze3d.opengl.GlBuffer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = GlBuffer.class)
public class GlBufferMixin implements GlBufferExt  {
	@Shadow
	@Final
	protected int handle;

	@Override
	public int cfi_getHandle() {
		return handle;
	}
}
