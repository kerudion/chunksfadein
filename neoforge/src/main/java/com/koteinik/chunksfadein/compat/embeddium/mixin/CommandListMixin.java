package com.koteinik.chunksfadein.compat.embeddium.mixin;

import com.koteinik.chunksfadein.compat.sodium.ext.CommandListExt;
import com.koteinik.chunksfadein.compat.sodium.ext.GlBufferUsageExt;
import com.koteinik.chunksfadein.compat.sodium.ext.GlMutableBufferExt;
import org.embeddedt.embeddium.impl.gl.buffer.GlBuffer;
import org.embeddedt.embeddium.impl.gl.buffer.GlBufferUsage;
import org.embeddedt.embeddium.impl.gl.buffer.GlMutableBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.nio.ByteBuffer;
import java.util.Map;

@Mixin(targets = "org.embeddedt.embeddium.impl.gl.device.GLRenderDevice$ImmediateCommandList", remap = false)
public abstract class CommandListMixin implements CommandListExt {
	@Shadow
	public abstract void uploadData(GlMutableBuffer glMutableBuffer, ByteBuffer byteBuffer, GlBufferUsage glBufferUsage);

	@Shadow
	public abstract GlMutableBuffer createMutableBuffer();

	@Shadow
	public abstract void deleteBuffer(GlBuffer glBuffer);

	private static final Map<GlBufferUsageExt, GlBufferUsage> glBufferUsageMap = Map.of(
		GlBufferUsageExt.STREAM_DRAW, GlBufferUsage.STREAM_DRAW,
		GlBufferUsageExt.STREAM_READ, GlBufferUsage.STREAM_READ,
		GlBufferUsageExt.STREAM_COPY, GlBufferUsage.STREAM_COPY,
		GlBufferUsageExt.STATIC_DRAW, GlBufferUsage.STATIC_DRAW,
		GlBufferUsageExt.STATIC_READ, GlBufferUsage.STATIC_READ,
		GlBufferUsageExt.STATIC_COPY, GlBufferUsage.STATIC_COPY,
		GlBufferUsageExt.DYNAMIC_DRAW, GlBufferUsage.DYNAMIC_DRAW,
		GlBufferUsageExt.DYNAMIC_READ, GlBufferUsage.DYNAMIC_READ,
		GlBufferUsageExt.DYNAMIC_COPY, GlBufferUsage.DYNAMIC_COPY
	);

	@Override
	public void uploadData(GlMutableBufferExt glBuffer, ByteBuffer buffer, GlBufferUsageExt glBufferUsage) {
		uploadData((GlMutableBuffer) glBuffer, buffer, glBufferUsageMap.get(glBufferUsage));
	}

	@Override
	public GlMutableBufferExt makeMutableBuffer() {
		return (GlMutableBufferExt) createMutableBuffer();
	}

	@Override
	public void deleteBuffer(GlMutableBufferExt chunkGlFadeDataBuffer) {
		deleteBuffer((GlBuffer) chunkGlFadeDataBuffer);
	}
}
