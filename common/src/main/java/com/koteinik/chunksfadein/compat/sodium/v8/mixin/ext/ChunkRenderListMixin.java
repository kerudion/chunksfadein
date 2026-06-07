package com.koteinik.chunksfadein.compat.sodium.v8.mixin.ext;

import com.koteinik.chunksfadein.compat.sodium.ext.ByteIteratorExt;
import com.koteinik.chunksfadein.compat.sodium.ext.ChunkRenderListExt;
import com.koteinik.chunksfadein.compat.sodium.ext.RenderRegionExt;
import net.caffeinemc.mods.sodium.client.render.chunk.lists.ChunkRenderList;
import net.caffeinemc.mods.sodium.client.render.chunk.region.RenderRegion;
import net.caffeinemc.mods.sodium.client.util.iterator.ByteIterator;
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
