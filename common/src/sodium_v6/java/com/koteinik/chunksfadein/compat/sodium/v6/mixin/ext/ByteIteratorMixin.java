package com.koteinik.chunksfadein.compat.sodium.v6.mixin.ext;

import com.koteinik.chunksfadein.compat.sodium.ext.ByteIteratorExt;
import net.caffeinemc.mods.sodium.client.util.iterator.ByteIterator;
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
