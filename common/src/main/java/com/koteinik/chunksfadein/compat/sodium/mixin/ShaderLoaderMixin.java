package com.koteinik.chunksfadein.compat.sodium.mixin;

import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.FadeMixType;
import com.koteinik.chunksfadein.core.FadeShader;
import com.koteinik.chunksfadein.core.FogOverrideMode;
import com.koteinik.chunksfadein.core.ShaderInjector;
import com.koteinik.chunksfadein.hooks.CompatibilityHook;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import me.jellysquid.mods.sodium.client.gl.shader.ShaderLoader;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = ShaderLoader.class, remap = false)
public abstract class ShaderLoaderMixin {
	@WrapMethod(method = "getShaderSource")
	private static String modifyConstructor(ResourceLocation name, Operation<String> original) {
		String source = original.call(name);
		if (source == null || CompatibilityHook.isIrisShaderPackInUse())
			return source;

		String path = name.getPath();

		String[] splittedPath = path.split("/");
		String shaderFileName = splittedPath[splittedPath.length - 1];

		switch (shaderFileName) {
			case "fog.glsl":
				source = prepareFogInjector().get(source);
				break;

			case "block_layer_opaque.fsh":
				source = prepareFragmentInjector().get(source);
				break;

			case "block_layer_opaque.vsh":
				source = prepareVertexInjector().get(source);
				break;

			default:
				break;
		}

		return source;
	}

	private static ShaderInjector prepareFogInjector() {
		ShaderInjector injector = new ShaderInjector();

		if (!Config.isModEnabled || !Config.isFadeEnabled || Config.fogOverrideMode == FogOverrideMode.NONE)
			return injector;

		if (Config.fadeMixType == FadeMixType.OKLAB)
			injector.replace(
				"mix(fragColor.rgb, fogColor.rgb, factor * fogColor.a)",
				"_cfi_mix_srgb_in_oklab(fragColor.rgb, fogColor.rgb, factor * fogColor.a)"
			);

		return injector;
	}

	private static ShaderInjector prepareFragmentInjector() {
		ShaderInjector injector = new ShaderInjector();
		FadeShader shader = new FadeShader();

		injector.insertAfterUniforms(shader.fragInVars().flushMultiline());

		if (!Config.isModEnabled || !Config.isFadeEnabled)
			return injector;

		injector.insertAfterStr("#version 330 core", shader.utilFunctions().flushMultiline());

		String inFogRange = switch (Config.fogOverrideMode) {
			case CYLINDRICAL -> "v_FragDistance > u_FogStart";
			case NONE -> "false";
		};

		injector.replace(
			"fragColor = _linearFog({color}, v_FragDistance, u_FogColor, u_FogStart, u_FogEnd);",
			"#ifdef USE_FOG",
			"vec3 fadeColor;",
			"vec4 fogColor = u_FogColor;",
			"if (cfi_FadeFactor < 1.0 || %s) {".formatted(inFogRange),
			"fadeColor = texture(cfi_sky, gl_FragCoord.xy / cfi_screenSize).rgb;",
			"if (%s) {".formatted(inFogRange),
			"fogColor.rgb = fadeColor;",
			"}",
			"}",
			"fragColor = _linearFog({color}, v_FragDistance, fogColor, u_FogStart, u_FogEnd);",
			shader.fragColorMod("{frag_color}.rgb", "fadeColor", true).flushMultiline(),
			"#else",
			"if (cfi_FadeFactor < 1.0) {",
			"vec3 fadeColor = texture(cfi_sky, gl_FragCoord.xy / cfi_screenSize).rgb;",
			shader.fragColorMod("{frag_color}.rgb", "fadeColor", false).flushMultiline(),
			"}",
			"else {",
			"{frag_color} = {color};",
			"}",
			"#endif"
		);

		return injector;
	}

	private static ShaderInjector prepareVertexInjector() {
		ShaderInjector injector = new ShaderInjector();
		FadeShader shader = new FadeShader();

		injector.insertAfterUniforms(shader
			.vertInVars()
			.vertOutVars()
			.flushMultiline());

		injector.insertAfterStr("#version 330 core", shader.utilFunctions().flushMultiline());

		injector.insertAfterStr(
			"_vert_init();",
			shader
				.newLine("vec3 cfi_position = _vert_position + u_RegionOffset + _get_draw_translation(_draw_id);")
				.worldToLocal("u_ModelViewMatrix")
				.vertInitOutVarsDrawId("_vert_position", "{mesh_id}")
				.vertInitMod("_vert_position", "cfi_position", "_vert_position", "vec3({mesh_id})", true)
				.flushMultiline()
		);

		return injector;
	}
}
