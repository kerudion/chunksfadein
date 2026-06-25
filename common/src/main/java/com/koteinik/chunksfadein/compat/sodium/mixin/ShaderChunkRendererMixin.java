package com.koteinik.chunksfadein.compat.sodium.mixin;

import com.koteinik.chunksfadein.ShaderUtils;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.pipeline.BindGroupLayout;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.UniformType;
import net.caffeinemc.mods.sodium.client.render.chunk.ShaderChunkRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;

@Mixin(value = ShaderChunkRenderer.class, remap = false)
public class ShaderChunkRendererMixin {
	@Shadow
	@Final
	private static Map<TerrainRenderPass, RenderPipeline> programs;

	static {
		ShaderUtils.setClearSodiumCache(programs::clear);
	}

	@WrapOperation(
		method = "<clinit>",
		at = @At(
			value = "INVOKE",
			target = "Lcom/mojang/blaze3d/pipeline/BindGroupLayout$Builder;build()Lcom/mojang/blaze3d/pipeline/BindGroupLayout;"
		)
	)
	private static BindGroupLayout cfi_injectBuffers(BindGroupLayout.Builder instance, Operation<BindGroupLayout> original) {
		return instance
			.withUniform("cfi_u_Globals", UniformType.UNIFORM_BUFFER)
			.withUniform("cfi_u_FadeData", UniformType.TEXEL_BUFFER, GpuFormat.RGBA32_FLOAT)
			.withSampler("cfi_sky")
			.build();
	}

	@WrapOperation(
		method = "createShader",
		at = @At(
			value = "INVOKE",
			target = "Lcom/mojang/blaze3d/pipeline/RenderPipeline$Builder;build()Lcom/mojang/blaze3d/pipeline/RenderPipeline;"
		)
	)
	private static RenderPipeline cfi_injectReloadThing(RenderPipeline.Builder instance, Operation<RenderPipeline> original) {
		return instance
			.withShaderDefine("CFI_CFG_VERSION", ShaderUtils.shaderVersion)
			.build();
	}
}
