package com.koteinik.chunksfadein.compat.sable.mixin;

import com.koteinik.chunksfadein.compat.sable.SableUtil;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.ryanhcode.sable.sublevel.render.SubLevelRenderData;
import dev.ryanhcode.sable.sublevel.render.vanilla.VanillaSingleSubLevelRenderData;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = VanillaSingleSubLevelRenderData.class, remap = false)
public abstract class VanillaSingleSubLevelRenderDataMixin implements SubLevelRenderData {
	@WrapOperation(
		method = "renderSingleBlock",
		at = @At(
			value = "INVOKE",
			target = "Lorg/joml/Matrix4f;translate(FFF)Lorg/joml/Matrix4f;"
		)
	)
	private Matrix4f cfi$applyCurvature(Matrix4f mat, float x, float y, float z, Operation<Matrix4f> op) {
		return op.call(mat, x, y + SableUtil.curvatureOffset(getSubLevel()), z);
	}
}
