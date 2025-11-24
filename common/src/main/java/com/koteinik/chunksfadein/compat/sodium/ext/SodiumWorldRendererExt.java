package com.koteinik.chunksfadein.compat.sodium.ext;

import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public interface SodiumWorldRendererExt {
	class Holder {
		public static SodiumWorldRendererExt instance = null;
	}

	float[] getAnimationOffset(Vec3 pos);

	@Nullable
	RenderSectionManagerExt getRenderSectionManager();
}
