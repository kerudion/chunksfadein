package com.koteinik.chunksfadein.core;

import com.koteinik.chunksfadein.compat.sodium.ext.CommandListExt;
import com.koteinik.chunksfadein.compat.sodium.ext.GlBufferUsageExt;
import com.koteinik.chunksfadein.compat.sodium.ext.GlMutableBufferExt;
import org.lwjgl.system.MemoryUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.function.LongPredicate;

import static org.lwjgl.system.Pointer.BITS32;
import static org.lwjgl.system.jni.JNINativeInterface.NewDirectByteBuffer;

public class DataBuffer {
	private static final sun.misc.Unsafe UNSAFE;
	private static long ADDRESS;

	static {
		UNSAFE = getUnsafeInstance();
		ADDRESS = getAddressOffset();
	}

	private static sun.misc.Unsafe getUnsafeInstance() {
		java.lang.reflect.Field[] fields = sun.misc.Unsafe.class.getDeclaredFields();

		for (java.lang.reflect.Field field : fields) {
			if (!field.getType().equals(sun.misc.Unsafe.class)) {
				continue;
			}

			int modifiers = field.getModifiers();
			if (!(java.lang.reflect.Modifier.isStatic(modifiers) && java.lang.reflect.Modifier.isFinal(modifiers))) {
				continue;
			}

			try {
				field.setAccessible(true);
				return (sun.misc.Unsafe) field.get(null);
			} catch (Exception ignored) {
			}
			break;
		}

		throw new UnsupportedOperationException("LWJGL requires sun.misc.Unsafe to be available.");
	}

	private static long getAddressOffset() {
		long MAGIC_ADDRESS = 0xDEADBEEF8BADF00DL & (BITS32 ? 0xFFFF_FFFFL : 0xFFFF_FFFF_FFFF_FFFFL);

		ByteBuffer bb = Objects.requireNonNull(NewDirectByteBuffer(MAGIC_ADDRESS, 0));

		return getFieldOffset(bb.getClass(), long.class, offset -> UNSAFE.getLong(bb, offset) == MAGIC_ADDRESS);
	}

	private static long getFieldOffset(Class<?> containerType, Class<?> fieldType, LongPredicate predicate) {
		Class<?> c = containerType;
		while (c != Object.class) {
			Field[] fields = c.getDeclaredFields();
			for (Field field : fields) {
				if (!field.getType().isAssignableFrom(fieldType) || Modifier.isStatic(field.getModifiers())
					|| field.isSynthetic()) {
					continue;
				}

				long offset = UNSAFE.objectFieldOffset(field);
				if (predicate.test(offset)) {
					return offset;
				}
			}
			c = c.getSuperclass();
		}
		throw new UnsupportedOperationException("Failed to find field offset in class.");
	}

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
		MemoryUtil.nmemFree(UNSAFE.getLong(buffer, ADDRESS));
	}

	private int getPosition(int index, int fieldNum) {
		return index * stride + fieldNum * 4;
	}
}
