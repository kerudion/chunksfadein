package com.koteinik.chunksfadein.compat.sodium.mixin;

import com.koteinik.chunksfadein.config.Config;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.koteinik.chunksfadein.core.FadeShader;
import com.koteinik.chunksfadein.core.ShaderInjector;
import com.koteinik.chunksfadein.hooks.CompatibilityHook;

import net.caffeinemc.mods.sodium.client.gl.shader.ShaderLoader;
import net.minecraft.resources.ResourceLocation;

@Mixin(value = ShaderLoader.class, remap = false)
public abstract class ShaderLoaderMixin {
	@Inject(method = "getShaderSource", at = @At("RETURN"), cancellable = true)
	private static void modifyConstructor(ResourceLocation name, CallbackInfoReturnable<String> cir) {
		if (CompatibilityHook.isIrisShaderPackInUse())
			return;

		String path = name.getPath();

		String[] splittedPath = path.split("/");
		String shaderFileName = splittedPath[splittedPath.length - 1];

		String source = cir.getReturnValue();

		switch (shaderFileName) {
			case "block_layer_opaque.fsh":
				source = prepareFragmentInjector().get(source);
				break;

			case "block_layer_opaque.vsh":
				source = prepareVertexInjector().get(source);
				break;

			default:
				break;
		}

		cir.setReturnValue(source);
	}

	private static ShaderInjector prepareFragmentInjector() {
		ShaderInjector injector = new ShaderInjector();
		FadeShader shader = new FadeShader();

		injector.insertAfterInVars(shader.fragInVars().flushMultiline());

		if (!Config.isModEnabled || !Config.isFadeEnabled)
			return injector;

		String inFogRange = switch (Config.fogOverrideMode) {
			case BOTH -> "v_FragDistance.x > u_RenderFog.x || v_FragDistance.y > u_EnvironmentFog.x";
			case CYLINDRICAL -> "v_FragDistance.x > u_RenderFog.x";
			case SPHERICAL -> "v_FragDistance.y > u_EnvironmentFog.x && v_FragDistance.x < u_RenderFog.x";
			case NONE -> "false";
		};

		injector.replace(
			"fragColor = _linearFog(diffuseColor, v_FragDistance, u_FogColor, u_EnvironmentFog, u_RenderFog);",
			"#ifdef USE_FOG",
			"vec3 fadeColor;",
			"vec4 fogColor = u_FogColor;",
			"if (cfi_FadeFactor < 1.0 || %s) {".formatted(inFogRange),
			"fadeColor = texture(cfi_sky, gl_FragCoord.xy / cfi_screenSize).rgb;",
			"if (%s) {".formatted(inFogRange),
			"fogColor.rgb = fadeColor;",
			"}",
			"}",
			"fragColor = _linearFog(diffuseColor, v_FragDistance, fogColor, u_EnvironmentFog, u_RenderFog);",
			shader.fragColorMod("{frag_color}.rgb", "fadeColor", true).flushMultiline(),
			"#else",
			"if (cfi_FadeFactor < 1.0) {",
			"vec3 fadeColor = texture(cfi_sky, gl_FragCoord.xy / cfi_screenSize).rgb;",
			shader.fragColorMod("{frag_color}.rgb", "fadeColor", false).flushMultiline(),
			"}",
			"else {",
			"{frag_color} = diffuseColor;",
			"}",
			"#endif"
		);

		return injector;
	}

	private static ShaderInjector prepareVertexInjector() {
		ShaderInjector injector = new ShaderInjector();
		FadeShader shader = new FadeShader();

		injector.insertAfterOutVars(shader
			.vertInVars()
			.vertOutVars()
			.flushMultiline());

		injector.insertAfterStr(
			"vec3 position",
			shader
				.vertInitOutVarsDrawId("_vert_position", "{mesh_id}")
				.vertInitMod("_vert_position", "position", false, "vec3({mesh_id})", true)
				.flushMultiline()
		);

		return injector;
	}
}
