package com.koteinik.chunksfadein.compat.dh.mixin;

import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.ShaderInjector;
import com.llamalad7.mixinextras.sugar.Local;
import com.seibel.distanthorizons.core.render.glObject.shader.ShaderProgram;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Supplier;

@Mixin(value = ShaderProgram.class, remap = false)
public class ShaderProgramMixin {
	@ModifyArg(method = "<init>(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;)V",
		at = @At(value = "INVOKE", target = "Lcom/seibel/distanthorizons/core/render/glObject/shader/ShaderProgram;<init>(Ljava/util/function/Supplier;Ljava/util/function/Supplier;Ljava/lang/String;[Ljava/lang/String;)V"),
		index = 1)
	private static Supplier<String> modifyFrag(Supplier<String> sourceSupplier, @Local(ordinal = 1, argsOnly = true) String frag) {
		return () -> {
			String source = sourceSupplier.get();
			if (!Config.isModEnabled || !Config.isFadeEnabled)
				return source;

			return (switch (frag) {
				case "shaders/ssao/ao.frag" -> prepareAOFragmentInjector();
				case "shaders/ssao/apply.frag" -> prepareAOApplyFragmentInjector();
				default -> ShaderInjector.EMPTY_INJECTOR;
			}).get(source);
		};
	}

	private static ShaderInjector prepareAOFragmentInjector() {
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
		ShaderInjector injector = new ShaderInjector();

		injector.replace("#version 150 core", "#version 330 core");

		injector.replace(
			"out vec4 fragColor;",
			"layout(location = 0) out vec4 fragColor;",
			"layout(location = 1) out vec4 cfi_terrainFadeOut;"
		);

		injector.insertAfterUniforms(
			"uniform sampler2D cfi_fadeTex;"
		);

		injector.appendToFunction(
			"main",
			"fragColor.rgb = vec3(0.0);",
			"fragColor.a = 1.0 - fragColor.a;",
			"cfi_terrainFadeOut = fragColor;",
			"fragColor.a *= texture(cfi_fadeTex, TexCoord).a;"
		);

		return injector;
	}
}
