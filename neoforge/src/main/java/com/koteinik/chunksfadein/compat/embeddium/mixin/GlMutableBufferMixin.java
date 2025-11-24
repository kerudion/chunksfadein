package com.koteinik.chunksfadein.compat.embeddium.mixin;

import com.koteinik.chunksfadein.compat.sodium.ext.GlMutableBufferExt;
import org.embeddedt.embeddium.impl.gl.buffer.GlMutableBuffer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = GlMutableBuffer.class, remap = false)
public class GlMutableBufferMixin implements GlMutableBufferExt {
}
