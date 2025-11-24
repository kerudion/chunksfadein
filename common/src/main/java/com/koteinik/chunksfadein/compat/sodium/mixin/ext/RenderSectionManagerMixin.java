package com.koteinik.chunksfadein.compat.sodium.mixin.ext;

import com.koteinik.chunksfadein.compat.sodium.ext.RenderSectionExt;
import com.koteinik.chunksfadein.compat.sodium.ext.RenderSectionManagerExt;
import com.koteinik.chunksfadein.config.Config;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSection;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = RenderSectionManager.class, remap = false)
public abstract class RenderSectionManagerMixin implements RenderSectionManagerExt {
	@Override
	public float[] getAnimationOffset(int x, int y, int z) {
		RenderSection section = getRenderSection(x, y, z);
		if (section == null)
			return null;

		return ((RenderSectionExt) section).getAnimationOffset();
	}

	@Override
	public float getFadeCoeff(int x, int y, int z) {
		RenderSection section = getRenderSection(x, y, z);
		if (section == null)
			return 0f;

		RenderSectionExt ext = (RenderSectionExt) section;
		if (ext.hasRenderedBefore() && !Config.isFadeEnabled)
			return 1f;

		return ext.getFadeCoeff();
	}

	@Shadow
	private RenderSection getRenderSection(int x, int y, int z) {
		return null;
	}
}
