package com.koteinik.chunksfadein.compat.dh.mixin.blaze;

import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.FadeShader;
import com.koteinik.chunksfadein.core.ShaderInjector;
import com.koteinik.chunksfadein.hooks.CompatibilityHook;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.shaders.ShaderType;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ShaderManager.class)
public abstract class ShaderManagerMixin {
	@ModifyReturnValue(
		method = "getShader(Lnet/minecraft/resources/Identifier;Lcom/mojang/blaze3d/shaders/ShaderType;)Ljava/lang/String;",
		at = @At("RETURN")
	)
	private String cfi_modifyDhBlazeShader(
		String original,
		@Local(argsOnly = true) Identifier id,
		@Local(argsOnly = true) ShaderType type
	) {
		if (!Config.isModEnabled || original == null || !"distanthorizons".equals(id.getNamespace()))
			return original;

		String path = id.getPath();
		if (type == ShaderType.VERTEX && "lod/blaze/vert".equals(path))
			return cfi_terrainVertexInjector().get(original);
		else if (type == ShaderType.FRAGMENT && "lod/blaze/frag".equals(path))
			return cfi_terrainFragmentInjector().get(original);
		else
			return original;
	}

	@Unique
	private static ShaderInjector cfi_terrainVertexInjector() {
		ShaderInjector injector = new ShaderInjector();
		FadeShader shader = new FadeShader();

		injector.replace("#version 150", "#version 330 core");

		if (Config.isFadeEnabled && !CompatibilityHook.isDHDitherEnabled())
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
				.vertInitMod("localPos", "vertexWorldPos", false, "offsetPos", true)
				// push water and lava slightly down
				.newLine("if (irisMaterial == 12 || irisMaterial == 6) { vertexWorldPos.y -= 0.115; }")
				.newLineIf(Config.isFadeEnabled && !CompatibilityHook.isDHDitherEnabled(), "cfi_material = irisMaterial;")
				.flushMultiline()
		);

		return injector;
	}

	@Unique
	private static ShaderInjector cfi_terrainFragmentInjector() {
		ShaderInjector injector = new ShaderInjector();
		FadeShader shader = new FadeShader();

		injector.insertAfterInVars(
			"uniform sampler3D cfi_lodMask;",
			"uniform vec3 cfi_lodMaskDim;",
			"uniform vec3 cfi_lodMaskMaxDist;",
			"uniform vec3 cfi_lodMaskOrigin;",
			"uniform float cfi_lodMaskMinY;",
			"uniform bool cfi_dhFadeActive;",
			"uniform float cfi_dhStartFadeBlockDistanceSq;"
		);

		if (Config.isFadeEnabled && !CompatibilityHook.isDHDitherEnabled())
			injector.insertAfterInVars("flat in int cfi_material;");

		injector.insertAfterInVars(
			shader.fragInVars()
				.utilFunctions()
				.flushMultiline()
		);

		shader.fragColorMod("fragColor.rgb", false);

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

		return injector;
	}
}
