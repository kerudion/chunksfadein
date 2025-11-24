package com.koteinik.chunksfadein.compat.sodium.mixin.ext;

import com.koteinik.chunksfadein.compat.sodium.ext.GlMutableBufferExt;
import net.caffeinemc.mods.sodium.client.gl.buffer.GlMutableBuffer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = GlMutableBuffer.class, remap = false)
public class GlMutableBufferMixin implements GlMutableBufferExt {
}
