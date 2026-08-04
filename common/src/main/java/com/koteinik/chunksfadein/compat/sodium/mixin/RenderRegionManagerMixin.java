package com.koteinik.chunksfadein.compat.sodium.mixin;

import com.koteinik.chunksfadein.compat.sodium.ext.RenderRegionManagerExt;
import net.caffeinemc.mods.sodium.client.render.chunk.IntPool;
import net.caffeinemc.mods.sodium.client.render.chunk.region.RenderRegionManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = RenderRegionManager.class, remap = false)
public class RenderRegionManagerMixin implements RenderRegionManagerExt {
	@Shadow
	@Final
	private IntPool freeIds;

	@Override
	public IntPool getFreeIds() {
		return freeIds;
	}
}
