package com.koteinik.chunksfadein.compat.embeddium.mixin;

import com.koteinik.chunksfadein.compat.sodium.ext.ByteIteratorExt;
import org.embeddedt.embeddium.impl.util.iterator.ByteIterator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ByteIterator.class, remap = false)
public interface ByteIteratorMixin extends ByteIteratorExt {
	@Shadow
	boolean hasNext();

	@Shadow
	int nextByteAsInt();

	default boolean next() {
		return hasNext();
	}

	default int getNext() {
		return nextByteAsInt();
	}
}
