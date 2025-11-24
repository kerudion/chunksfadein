package com.koteinik.chunksfadein.core;

import com.koteinik.chunksfadein.compat.sodium.ext.CommandListExt;
import com.koteinik.chunksfadein.compat.sodium.ext.GlBufferUsageExt;
import com.koteinik.chunksfadein.compat.sodium.ext.GlMutableBufferExt;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public class DataBuffer {
	private final ByteBuffer buffer;
	private final int stride;
	private final int fieldsCount;

	public DataBuffer(int size, int fieldsCount) {
		this.stride = fieldsCount * 4;
		this.fieldsCount = fieldsCount;

		buffer = MemoryUtil.memAlloc(size * stride);

		for (int i = 0; i < size; i++)
			for (int j = 0; j < fieldsCount; j++)
				put(i, j, 0f);
	}

	public void put(int index, int fieldNum, float data) {
		buffer.putFloat(getPosition(index, fieldNum), data);
	}

	public float get(int index, int fieldNum) {
		return buffer.getFloat(getPosition(index, fieldNum));
	}

	public void uploadData(CommandListExt commandList, GlMutableBufferExt glBuffer) {
		commandList.uploadData(glBuffer, buffer, GlBufferUsageExt.STREAM_DRAW);
	}

	public void reset(int i) {
		for (int j = 0; j < fieldsCount; j++)
			put(i, j, 0f);
	}

	public void delete() {
		MemoryUtil.memFree(buffer);
	}

	private int getPosition(int index, int fieldNum) {
		return index * stride + fieldNum * 4;
	}
}
