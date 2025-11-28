package com.koteinik.chunksfadein.compat.iris.mixin;

import com.koteinik.chunksfadein.compat.iris.IrisPatcher;
import com.llamalad7.mixinextras.sugar.Local;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import net.irisshaders.iris.pipeline.transform.TransformPatcher;
import net.irisshaders.iris.pipeline.transform.parameter.Parameters;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.EnumMap;
import java.util.Map;

@Mixin(value = TransformPatcher.class, remap = false)
public class TransformPatcherMixin {
	@Inject(
		method = "transform",
		at = @At(
			value = "INVOKE",
			target = "Lnet/irisshaders/iris/pipeline/transform/TransformPatcher;transformInternal(Ljava/lang/String;Ljava/util/Map;Lnet/irisshaders/iris/pipeline/transform/parameter/Parameters;)Ljava/util/Map;"
		)
	)
	private static void modifyTransformation(
		String name,
		String vertex,
		String geometry,
		String tessControl,
		String tessEval,
		String fragment,
		Parameters parameters,
		CallbackInfoReturnable<Map<PatchShaderType, String>> cir,
		@Local EnumMap<PatchShaderType, String> inputs
	) {
		IrisPatcher.currentPipelineShaders.set(
			inputs.entrySet().stream()
				.filter(e -> e.getValue() != null)
				.map(Map.Entry::getKey)
				.toList()
		);
	}
}
