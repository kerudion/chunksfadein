package com.koteinik.chunksfadein.mixin.dh.iris;

import com.koteinik.chunksfadein.core.FadeShader;
import com.koteinik.chunksfadein.core.IrisPatcher;
import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.transform.ASTInjectionPoint;
import io.github.douira.glsl_transformer.ast.transform.ASTParser;
import net.irisshaders.iris.pipeline.transform.parameter.Parameters;
import net.irisshaders.iris.pipeline.transform.transformer.DHTerrainTransformer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = DHTerrainTransformer.class, remap = false)
public class DHTerrainTransformerMixin {
	@Inject(method = "transform", at = @At("TAIL"))
	private static void modifyTransform(ASTParser t, TranslationUnit tree, Root root, Parameters parameters, CallbackInfo ci) {
		FadeShader shader = new FadeShader();

		if (IrisPatcher.hasFn(tree, "_cfi_noInjectMarker") || IrisPatcher.hasFn(tree, "_cfi_ignoreMarker")) return;

		switch (parameters.type) {
			case VERTEX -> {
				shader.newLine("vec3 _cfi_worldPos;");
				tree.parseAndInjectNodes(
					t, ASTInjectionPoint.BEFORE_FUNCTIONS,
					shader.flushArray()
				);

				shader.newLine("_cfi_worldPos = _vert_position + modelOffset;");
				shader.newLine("cfi_localPos = _vert_position;");
				shader.newLine("cfi_worldPos = _cfi_worldPos;");
				shader.newLine("cfi_initOutVars();");
				tree.appendFunctionBody(
					"_vert_init",
					t.parseStatements(root, shader.flushArray())
				);

				tree.parseAndInjectNodes(
					t, ASTInjectionPoint.BEFORE_FUNCTIONS,
					shader.dhUniforms().flushArray()
				);

				tree.parseAndInjectNodes(
					t, ASTInjectionPoint.BEFORE_FUNCTIONS,
					shader.dhVertOutVars().flushArray()
				);

				tree.parseAndInjectNodes(
					t, ASTInjectionPoint.BEFORE_FUNCTIONS,
					shader.dhApiGetFadeData().flushSingleLine(),

					shader.dhApiVertCalculateDisplacement().flushSingleLine(),
					shader.dhApiVertCalculateDisplacement2().flushSingleLine(),

					shader.apiVertCalculateCurvature().flushSingleLine(),
					shader.apiVertCalculateCurvature2().flushSingleLine(),

					shader.dhApiVertInitOutVars().flushSingleLine(),
					shader.dhApiVertInitOutVars2().flushSingleLine()
				);
			}
			case FRAGMENT -> {
				tree.parseAndInjectNodes(
					t, ASTInjectionPoint.BEFORE_FUNCTIONS,
					shader.dhUniforms().flushArray()
				);

				tree.parseAndInjectNodes(
					t, ASTInjectionPoint.BEFORE_FUNCTIONS,
					shader.dhFragInVars().flushArray()
				);

				tree.parseAndInjectNodes(
					t, ASTInjectionPoint.BEFORE_FUNCTIONS,

					shader.dhApiLodIsMasked().flushSingleLine(),
					shader.dhApiLodIsMasked2().flushSingleLine(),

					shader.dhApiGetFadeData().flushSingleLine(),
					shader.apiFragApplyFogFade().flushSingleLine(),
					shader.apiFragApplyFade().flushSingleLine(),
					shader.apiFragCalculateFade().flushSingleLine()
				);

			}
			default -> {
			}
		}

		IrisPatcher.sortUses(tree);
	}
}
