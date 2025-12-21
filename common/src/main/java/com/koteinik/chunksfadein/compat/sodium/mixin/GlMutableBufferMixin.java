package com.koteinik.chunksfadein.compat.sodium.mixin;

import com.koteinik.chunksfadein.compat.sodium.ext.GlMutableBufferExt;
import me.jellysquid.mods.sodium.client.gl.buffer.GlMutableBuffer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = GlMutableBuffer.class, remap = false)
public class GlMutableBufferMixin implements GlMutableBufferExt {
}
