package com.koteinik.chunksfadein.compat.dh.mixin.blaze;

import com.koteinik.chunksfadein.compat.dh.ext.RenderPassWrapperExt;
import com.mojang.blaze3d.systems.RenderPass;
import com.seibel.distanthorizons.common.render.blaze.wrappers.RenderPassWrapper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = RenderPassWrapper.class, remap = false)
public class RenderPassWrapperMixin implements RenderPassWrapperExt {
	@Shadow
	@Final
	private RenderPass renderPass;

	@Override
	public RenderPass cfi_getPass() {
		return renderPass;
	}
}
