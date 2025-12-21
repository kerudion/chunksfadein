package com.koteinik.chunksfadein.compat.sodium.ext;

import java.nio.ByteBuffer;

public interface CommandListExt {
	void uploadData(GlMutableBufferExt glBuffer, ByteBuffer buffer, GlBufferUsageExt glBufferUsage);

	GlMutableBufferExt makeMutableBuffer();

	void deleteBuffer(GlMutableBufferExt chunkGlFadeDataBuffer);
}
