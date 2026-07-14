package com.koteinik.chunksfadein.compat.dh.mixin;

import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.FadeShader;
import com.koteinik.chunksfadein.core.ShaderInjector;
import com.koteinik.chunksfadein.hooks.CompatibilityHook;
import com.llamalad7.mixinextras.sugar.Local;
import com.seibel.distanthorizons.common.render.openGl.glObject.shader.GlShaderProgram;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = GlShaderProgram.class, remap = false)
public class GlShaderProgramMixin {
	@ModifyArg(
		method = "<init>(Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;)V",
		at = @At(
			value = "INVOKE",
			target = "Lcom/seibel/distanthorizons/common/render/openGl/glObject/shader/GlShader;<init>(ILjava/lang/String;)V",
			ordinal = 0
		),
		index = 1
	)
	private String modifySourceVert(
		String sauce,
		@Local(argsOnly = true, ordinal = 0) String vertResourcePath
	) {
		if (!Config.isModEnabled) return sauce;
		if (!vertResourcePath.equals("assets/distanthorizons/shaders/terrain/gl/vert.vert")) return sauce;

		return prepareTerrainVertexInjector().get(sauce);
	}

	@ModifyArg(
		method = "<init>(Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;)V",
		at = @At(
			value = "INVOKE",
			target = "Lcom/seibel/distanthorizons/common/render/openGl/glObject/shader/GlShader;<init>(ILjava/lang/String;)V",
			ordinal = 1
		),
		index = 1
	)
	private String modifySourceFrag(
		String sauce,
		@Local(argsOnly = true, ordinal = 1) String fragResourcePath
	) {
		if (!Config.isModEnabled) return sauce;
		String source = (switch (fragResourcePath) {
			case "assets/distanthorizons/shaders/ssao/gl/ao.frag" -> prepareAOFragmentInjector();
			case "assets/distanthorizons/shaders/ssao/gl/apply.frag" -> prepareAOApplyFragmentInjector();
			case "assets/distanthorizons/shaders/terrain/gl/frag.frag" -> prepareTerrainFragmentInjector();
			default -> ShaderInjector.EMPTY_INJECTOR;
		}).get(sauce);

		return source;
	}


	@Unique
	private static ShaderInjector prepareTerrainVertexInjector() {
		ShaderInjector injector = new ShaderInjector();
		FadeShader shader = new FadeShader();

		injector.replace("#version 150 core", "#version 330 core");

		if (Config.isFadeEnabled)
			injector.insertAfterOutVars("flat out int cfi_material;");

		injector.insertAfterOutVars(shader
			.newLine("uniform vec4 cfi_chunkFadeData;")
			.newLine("uniform vec3 cfi_lodMaskOrigin;")
			.vertOutVars()
			.utilFunctions()
			.flushMultiline());

		injector.insertAfterStr(
			"vertexWorldPos = ",
			shader
				.newLine("vec4 chunkFadeData = cfi_chunkFadeData;")
				.newLine("vec3 localPos = vec3(vPosition.xyz);")
				.newLine("vec3 offsetPos = floor((vertexWorldPos - mod(localPos, 16.0)) / 16.0) + cfi_lodMaskOrigin;")
				.vertInitOutVars("localPos", "offsetPos")
				.vertInitMod("localPos", "vertexWorldPos", "vertexWorldPos", "offsetPos", true)
				// push water and lava slightly down
				.newLine("if (irisData.x == 12u || irisData.x == 6u) { vertexWorldPos.y -= 0.115; }")
				.newLineIf(Config.isFadeEnabled, "cfi_material = int(irisData.x);")
				.flushMultiline()
		);

		return injector;
	}

	@Unique
	private static ShaderInjector prepareTerrainFragmentInjector() {
		ShaderInjector injector = new ShaderInjector();
		FadeShader shader = new FadeShader();

		injector.insertAfterInVars(
			shader.dhSamplers()
				.dhUniforms()
				.flushMultiline()
		);

		if (Config.isFadeEnabled)
			injector.insertAfterInVars("flat in int cfi_material;");

		injector.insertAfterInVars(
			shader.fragInVars()
				.utilFunctions()
				.flushMultiline()
		);

		if (Config.isFadeEnabled) {
			injector.replace("#version 150", "#version 330 core");

			injector.replace(
				"out vec4 fragColor;",
				"layout(location = 0) out vec4 fragColor;",
				CompatibilityHook.isDHSSAOEnabled()
					? "layout(location = 1) out vec4 cfi_terrainFadeOut;"
					: ""
			);
		}

		shader.fragColorMod("fragColor.rgb", false);

		if (Config.isFadeEnabled && CompatibilityHook.isDHSSAOEnabled())
			shader.newLine("cfi_terrainFadeOut.a = fade;");

		if (CompatibilityHook.isDHDitherEnabled()) {
			shader.newLine("bool allowDither = !uDitherDhRendering;");
			shader.dhMaskLod("allowDither = true;", "vPos", "vertexWorldPos", true);

			injector.replace(
				"if (uDitherDhRendering)",
				"if (allowDither)"
			);
		} else {
			String whenOccluded = Config.isFadeEnabled ? "if (cfi_material != 12) { discard; }" : "discard;";
			shader.dhMaskLod(whenOccluded, "vPos", "vertexWorldPos", true);
		}

		injector.replace(
			"viewDist < uClipDistance && uClipDistance > 0.0",
			"false"
		);

		injector.insertAfterStr(
			"fragColor = vertexColor;",
			shader.flushMultiline()
		);

		if (Config.isFadeEnabled && CompatibilityHook.isDHSSAOEnabled())
			shader.newLine("cfi_terrainFadeOut.rgb = texture(cfi_sky, gl_FragCoord.xy / cfi_screenSize).rgb;");

		injector.appendToFunction(
			"main",
			shader.flushMultiline()
		);

		return injector;
	}


	private static ShaderInjector prepareAOFragmentInjector() {
		if (!Config.isFadeEnabled) return ShaderInjector.EMPTY_INJECTOR;

		ShaderInjector injector = new ShaderInjector();

		injector.insertAfterUniforms(
			"uniform sampler2D cfi_fadeTex;"
		);

		injector.insertAfterStr(
			"occlusion = ",
			"occlusion *= texture(cfi_fadeTex, TexCoord).a;"
		);

		return injector;
	}

	private static ShaderInjector prepareAOApplyFragmentInjector() {
		if (!Config.isFadeEnabled) return ShaderInjector.EMPTY_INJECTOR;

		ShaderInjector injector = new ShaderInjector();

		injector.replace("#version 150 core", "#version 330 core");

		injector.replace(
			"out vec4 fragColor;",
			"layout(location = 0) out vec4 fragColor;"
		);

		injector.insertAfterUniforms(
			"uniform sampler2D cfi_fadeTex;"
		);

		injector.appendToFunction(
			"main",
			"fragColor.rgb = vec3(0.0);",
			"fragColor.a = 1.0 - fragColor.a;",
			"fragColor.a *= texture(cfi_fadeTex, TexCoord).a;"
		);

		return injector;
	}
}
