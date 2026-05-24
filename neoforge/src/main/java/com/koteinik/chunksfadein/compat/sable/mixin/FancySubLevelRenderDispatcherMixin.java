package com.koteinik.chunksfadein.compat.sable.mixin;

import com.koteinik.chunksfadein.compat.sable.SableUtil;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.ryanhcode.sable.sublevel.render.dispatcher.FancySubLevelRenderDispatcher;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = FancySubLevelRenderDispatcher.class, remap = false)
public class FancySubLevelRenderDispatcherMixin {
	@WrapOperation(
		method = "renderSectionLayer",
		at = @At(
			value = "INVOKE",
			target = "Lorg/joml/Matrix4f;translate(FFF)Lorg/joml/Matrix4f;"
		)
	)
	private Matrix4f cfi$applyCurvature(
		Matrix4f mat,
		float x, float y, float z,
		Operation<Matrix4f> op,
		@Local ClientSubLevel subLevel
	) {
		return op.call(mat, x, y + SableUtil.curvatureOffset(subLevel), z);
	}
}
