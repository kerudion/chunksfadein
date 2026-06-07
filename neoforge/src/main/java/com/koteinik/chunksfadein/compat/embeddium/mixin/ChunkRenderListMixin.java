package com.koteinik.chunksfadein.compat.embeddium.mixin;

import com.koteinik.chunksfadein.compat.sodium.ext.ByteIteratorExt;
import com.koteinik.chunksfadein.compat.sodium.ext.ChunkRenderListExt;
import com.koteinik.chunksfadein.compat.sodium.ext.RenderRegionExt;
import org.embeddedt.embeddium.impl.render.chunk.lists.ChunkRenderList;
import org.embeddedt.embeddium.impl.render.chunk.region.RenderRegion;
import org.embeddedt.embeddium.impl.util.iterator.ByteIterator;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ChunkRenderList.class, remap = false)
public abstract class ChunkRenderListMixin implements ChunkRenderListExt {
	@Shadow
	@Nullable
	public abstract ByteIterator sectionsWithGeometryIterator(boolean reverse);

	@Shadow
	public abstract RenderRegion getRegion();

	@Override
	public ByteIteratorExt getSectionsWithGeometryIterator(boolean reverse) {
		return (ByteIteratorExt) sectionsWithGeometryIterator(reverse);
	}

	@Override
	public RenderRegionExt region() {
		return (RenderRegionExt) getRegion();
	}
}
