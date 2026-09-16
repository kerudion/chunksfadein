package com.koteinik.chunksfadein.compat.sodium.mixin;

import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.FadeMixType;
import com.koteinik.chunksfadein.core.FadeShader;
import com.koteinik.chunksfadein.core.FogOverrideMode;
import com.koteinik.chunksfadein.core.ShaderInjector;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.renderpearl.api.pipeline.ShaderType;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ShaderManager.Configs.class)
public class ShaderManagerMixin {
	@ModifyReturnValue(method = "getShader", at = @At("RETURN"))
	private String cfi_injectShaders(String source, @Local(argsOnly = true) Identifier location, @Local(argsOnly = true) ShaderType type) {
		if (source == null)
			return source;

		String path = location.getPath();

		String[] splittedPath = path.split("/");
		String shaderFileName = splittedPath[splittedPath.length - 1];
		if (!shaderFileName.startsWith("block_layer_opaque"))
			return source;

		switch (type) {
			case FRAGMENT:
				source = prepareFragmentInjector().get(source);
				break;

			case VERTEX:
				source = prepareVertexInjector().get(source);
				break;

			default:
				break;
		}

		return source;
	}

	private static ShaderInjector prepareFragmentInjector() {
		ShaderInjector injector = new ShaderInjector();
		FadeShader shader = new FadeShader().baseLocation(8);

		injector.insertAfterInVars(shader.fragInVars().flushMultiline());

		if (!Config.isModEnabled || !Config.isFadeEnabled)
			return injector;

		injector.insertAfterVersion(shader.utilFunctions().flushMultiline());

		String inFogRange = switch (Config.fogOverrideMode) {
			case BOTH -> "v_FragDistance.x > u_RenderFog.x || v_FragDistance.y > u_EnvironmentFog.x";
			case CYLINDRICAL -> "v_FragDistance.x > u_RenderFog.x";
			case SPHERICAL -> "v_FragDistance.y > u_EnvironmentFog.x && v_FragDistance.x < u_RenderFog.x";
			case NONE -> "false";
		};

		String applyFog;
		if (Config.fogOverrideMode != FogOverrideMode.NONE && Config.fadeMixType == FadeMixType.OKLAB) {
			applyFog = "float cfi_fogValue = total_fog_value(v_FragDistance.y, v_FragDistance.x, u_EnvironmentFog.x, u_EnvironmentFog.y, u_RenderFog.x, u_RenderFog.y);";
			applyFog += "\ncolor = vec4(_cfi_mix_srgb_in_oklab({color}.rgb, fogColor.rgb, cfi_fogValue * fogColor.a), {color}.a);";
		} else {
			applyFog = "color = _linearFog({color}, v_FragDistance, fogColor, u_EnvironmentFog, u_RenderFog, 1.0);";
		}

		injector.replace(
			"return _linearFog(color, v_FragDistance, fogColor, u_EnvironmentFog, u_RenderFog, factor);",
			"vec3 fadeColor;",
			"if (cfi_FadeFactor < 1.0 || %s) {".formatted(inFogRange),
			"fadeColor = texture(cfi_sky, gl_FragCoord.xy / cfi_screenSize).rgb;",
			"#ifdef OIT_ACCUMULATE",
			"fadeColor *= {color}.a;",
			"#endif",
			"if (%s) {".formatted(inFogRange),
			"fogColor.rgb = fadeColor;",
			"}",
			"}",
			applyFog,
			shader.fragColorMod("color.rgb", "fadeColor", true).flushMultiline(),
			"return color;"
		);

		return injector;
	}

	private static ShaderInjector prepareVertexInjector() {
		ShaderInjector injector = new ShaderInjector();
		FadeShader shader = new FadeShader().baseLocation(8);

		injector.insertAfterStr(
			"uniform isamplerBuffer u_SectionTimeInfo;",
			shader
				.vertInVars()
				.vertOutVars()
				.flushMultiline()
		);

		injector.insertAfterVersion(shader.utilFunctions().flushMultiline());

		injector.insertAfterStr(
			"_vert_init();",
			shader
				.newLine("vec3 cfi_position = _vert_position + u_RegionOffset + _get_draw_translation(_draw_id);")
				.vertInitOutVarsDrawId("_vert_position", "{mesh_id}")
				.vertInitMod("_vert_position", "cfi_position", true, "vec3({mesh_id})", true)
				.flushMultiline()
		);

		if (Config.isModEnabled)
			injector.replace(
				"fadeFactor = (chunkFade < 0) ? 1.0 : fade;",
				"fadeFactor = 1.0;"
			);

		return injector;
	}
}
