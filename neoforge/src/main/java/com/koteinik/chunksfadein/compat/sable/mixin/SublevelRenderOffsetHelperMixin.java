package com.koteinik.chunksfadein.compat.sable.mixin;

import com.koteinik.chunksfadein.compat.sable.SableUtil;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.util.SublevelRenderOffsetHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = SublevelRenderOffsetHelper.class, remap = false)
public class SublevelRenderOffsetHelperMixin {
	@WrapOperation(
		method = "posePlotToProjected",
		at = @At(
			value = "INVOKE",
			ordinal = 0,
			target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(DDD)V"
		)
	)
	private static void cfi$applyCurvature(
		PoseStack ps,
		double x, double y, double z,
		Operation<Void> op,
		@Local(argsOnly = true) SubLevel subLevel
	) {
		op.call(ps, x, y + SableUtil.curvatureOffset(subLevel), z);
	}
}
