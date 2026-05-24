package com.koteinik.chunksfadein.compat.sable.mixin;

import com.koteinik.chunksfadein.compat.sable.SableUtil;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.render.SubLevelRenderData;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = SubLevelRenderData.class, remap = false)
public interface SubLevelRenderDataMixin {
	@WrapOperation(
		method = "getTransformation(DDDLorg/joml/Matrix4f;)Lorg/joml/Matrix4f;",
		at = @At(
			value = "INVOKE",
			target = "Lorg/joml/Matrix4f;translate(FFF)Lorg/joml/Matrix4f;"
		)
	)
	default Matrix4f cfi$applyCurvature(Matrix4f store, float x, float y, float z, Operation<Matrix4f> op) {
		SubLevel sub = ((SubLevelRenderData) this).getSubLevel();
		return op.call(store, x, y + SableUtil.curvatureOffset(sub), z);
	}
}
