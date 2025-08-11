package com.koteinik.chunksfadein.compat.iris.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.koteinik.chunksfadein.compat.iris.IrisPatcher;
import com.llamalad7.mixinextras.sugar.Local;

import net.irisshaders.iris.shaderpack.ShaderPack;
import net.irisshaders.iris.shaderpack.include.AbsolutePackPath;
import net.irisshaders.iris.shaderpack.include.IncludeProcessor;

@Mixin(value = ShaderPack.class, remap = false)
public class ShaderPackMixin {
	@Inject(
		method = "lambda$new$8",
		at = @At(
			value = "INVOKE",
			target = "Lnet/irisshaders/iris/shaderpack/preprocessor/JcppProcessor;glslPreprocessSource",
			shift = Shift.BEFORE))
	private static void modifyInit(
		List<?> list, IncludeProcessor includeProcessor, Iterable<?> iterable, AbsolutePackPath path, CallbackInfoReturnable<?> ci,
		@Local(name = "programString") String programString) {
		IrisPatcher.currentShaderName.set(programString.contains("/") ? programString.split("/")[1] : programString);
	}
}
