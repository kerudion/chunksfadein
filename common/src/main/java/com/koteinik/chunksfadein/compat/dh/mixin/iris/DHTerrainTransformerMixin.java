package com.koteinik.chunksfadein.compat.dh.mixin.iris;

import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.FadeShader;
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

import static com.koteinik.chunksfadein.compat.iris.IrisPatcher.*;

@Mixin(value = DHTerrainTransformer.class, remap = false)
public class DHTerrainTransformerMixin {
	@Inject(method = "transform", at = @At("TAIL"))
	private static void modifyTransform(ASTParser t, TranslationUnit tree, Root root, Parameters parameters, CallbackInfo ci) {
		FadeShader shader = new FadeShader();

		if (hasDef(tree, "_cfi_noInjectMarker") || hasDef(tree, "_cfi_ignoreMarker")) return;

		boolean apiUsed = hasDef(tree, "_cfi_usedMarker");
		if (!apiUsed) {
			if (!Config.patchShaderFade)
				shader.overrideFade(false);
			if (!Config.patchShaderAnimation)
				shader.overrideAnimation(false);
			if (!Config.patchShaderCurvature)
				shader.overrideCurvature(false);
		}

		boolean inject = !hasDef(tree, "_cfi_noInjectMarker");
		boolean injectMod = !hasDef(tree, "_cfi_noInjectModMarker");
		boolean injectFragMod = injectMod && !hasDef(tree, "_cfi_noInjectFragModMarker");
		boolean injectVertMod = injectMod && !hasDef(tree, "_cfi_noInjectVertModMarker");
		boolean injectCurvature = injectMod && !hasDef(tree, "_cfi_noCurvatureMarker");
		boolean injectLodMask = injectMod && !hasDef(tree, "_cfi_noLodMaskMarker");

		switch (parameters.type) {
			case VERTEX -> {
				shader.newLine("vec3 _cfi_worldPos;");
				tree.parseAndInjectNodes(
					t, ASTInjectionPoint.BEFORE_FUNCTIONS,
					shader.flushArray()
				);

				tree.parseAndInjectNodes(
					t, ASTInjectionPoint.BEFORE_FUNCTIONS,
					shader.dhUniforms().flushArray()
				);

				tree.parseAndInjectNodes(
					t, ASTInjectionPoint.BEFORE_FUNCTIONS,
					shader.utilRand().flushSingleLine(),
					shader.utilSrgbToOklab().flushSingleLine(),
					shader.utilOklabToSrgb().flushSingleLine(),
					shader.utilMixSrgbInOklab().flushSingleLine()
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

				shader.newLine("_cfi_worldPos = _vert_position + modelOffset;");

				if (!inject) break;

				shader.newLine("cfi_initOutVars();");

				shader.newLine("cfi_localPos = _vert_position;");
				shader.newLine("cfi_worldPos = _cfi_worldPos;");

				if (injectVertMod) {
					shader.newLine("_vert_position += cfi_calculateDisplacement();");
					if (injectCurvature)
						shader.newLine("_vert_position += cfi_calculateCurvature();");
					shader.newLine("if (irisExtra.x == 12.0 || irisExtra.x == 6.0) { _vert_position.y -= 0.115; }");
				}

				tree.appendFunctionBody(
					"_vert_init",
					t.parseStatements(root, shader.flushArray())
				);

				tree.parseAndInjectNodes(
					t, ASTInjectionPoint.BEFORE_FUNCTIONS,
					shader.dhVertOutVars().flushArray()
				);
			}
			case FRAGMENT -> {
				tree.parseAndInjectNodes(
					t, ASTInjectionPoint.BEFORE_FUNCTIONS,
					shader.dhSamplers().dhUniforms().flushArray()
				);

				tree.parseAndInjectNodes(
					t, ASTInjectionPoint.BEFORE_FUNCTIONS,
					shader.dhFragInVars().flushArray()
				);

				tree.parseAndInjectNodes(
					t, ASTInjectionPoint.BEFORE_FUNCTIONS,
					shader.utilRand().flushSingleLine(),
					shader.utilSrgbToOklab().flushSingleLine(),
					shader.utilOklabToSrgb().flushSingleLine(),
					shader.utilMixSrgbInOklab().flushSingleLine()
				);

				tree.parseAndInjectNodes(
					t, ASTInjectionPoint.BEFORE_FUNCTIONS,

					shader.dhApiLodIsMasked().flushSingleLine(),
					shader.dhApiLodIsMasked2().flushSingleLine(),

					shader.dhApiGetFadeData().flushSingleLine(),
					shader.apiFragApplyFogFade().flushSingleLine(),
					shader.apiFragApplyFade().flushSingleLine(),
					shader.apiFragCalculateFade().flushSingleLine(),
					shader.apiFragSampleSkyLodTexture().flushSingleLine()
				);

				if (injectFragMod)
					injectFragMod(shader, t, tree, root);
			}
			default -> {
			}
		}

		sortUses(tree);
	}
}
