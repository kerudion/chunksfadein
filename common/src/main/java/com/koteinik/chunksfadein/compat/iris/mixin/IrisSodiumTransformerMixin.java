package com.koteinik.chunksfadein.compat.iris.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.koteinik.chunksfadein.compat.iris.IrisPatcher;

import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.transform.ASTParser;
import net.irisshaders.iris.pipeline.transform.parameter.SodiumParameters;
import net.irisshaders.iris.pipeline.transform.transformer.SodiumTransformer;

@Mixin(value = SodiumTransformer.class, remap = false)
public class IrisSodiumTransformerMixin {
	@Inject(method = "transform", at = @At("TAIL"))
	private static void modifyTransform(ASTParser t, TranslationUnit tree, Root root, SodiumParameters parameters, CallbackInfo ci) {
		IrisPatcher.injectModAndAPI(t, tree, root, parameters);
	}
}
