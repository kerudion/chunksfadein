package com.koteinik.chunksfadein.mixin.shader;

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

		injector.replace(
			"fragColor = _linearFog(diffuseColor, v_FragDistance, u_FogColor, u_FogStart, u_FogEnd);",
			"#ifdef USE_FOG",
			"if (cfi_FadeFactor < 1.0 || v_FragDistance > u_FogStart) {",
			"vec3 fadeColor = texture(cfi_sky, gl_FragCoord.xy / cfi_screenSize).rgb;",
			"fragColor = _linearFog(diffuseColor, v_FragDistance, vec4(fadeColor, u_FogColor.a), u_FogStart, u_FogEnd);",
			shader.fragColorMod("{frag_color}.rgb", "fadeColor", false).flushMultiline(),
			"}",
			"#else",
			"if (cfi_FadeFactor < 1.0) {",
			"vec3 fadeColor = texture(cfi_sky, gl_FragCoord.xy / cfi_screenSize).rgb;",
			shader.fragColorMod("{frag_color}.rgb", "fadeColor", false).flushMultiline(),
			"}",
			"#endif",
			"else {",
			"{frag_color} = diffuseColor;",
			"}"
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
