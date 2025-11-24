package com.koteinik.chunksfadein.compat.dh.mixin;

import com.koteinik.chunksfadein.config.Config;
import com.seibel.distanthorizons.core.util.RenderUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = RenderUtil.class, remap = false)
public class RenderUtilMixin {
	@ModifyVariable(method = "getNearClipPlaneDistanceInBlocks(FF)F", at = @At(value = "RETURN"), name = "nearClipPlane", ordinal = 0)
	private static float modifyGetNearClipPlaneDistanceInBlocks(float value) {
		if (Config.isModEnabled)
			return 0f;
		else
			return value;
	}
}
