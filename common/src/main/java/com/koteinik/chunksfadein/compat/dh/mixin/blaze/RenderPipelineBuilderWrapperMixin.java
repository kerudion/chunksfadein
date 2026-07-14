package com.koteinik.chunksfadein.compat.dh.mixin.blaze;

import com.koteinik.chunksfadein.ShaderUtils;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.textures.TextureFormat;
import com.seibel.distanthorizons.common.render.blaze.wrappers.RenderPipelineBuilderWrapper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = RenderPipelineBuilderWrapper.class, remap = false)
public class RenderPipelineBuilderWrapperMixin {
	@Shadow
	@Final
	private RenderPipeline.Builder blazePipelineBuilder;

	@Inject(method = "build", at = @At("HEAD"))
	private void cfi_addVersionDefine(CallbackInfoReturnable<RenderPipeline> cir) {
		blazePipelineBuilder.withShaderDefine("CFI_CFG_VERSION", ShaderUtils.shaderVersion);
	}

	@WrapOperation(method = "withUniformBuffer", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/pipeline/RenderPipeline$Builder;withUniform(Ljava/lang/String;Lcom/mojang/blaze3d/shaders/UniformType;)Lcom/mojang/blaze3d/pipeline/RenderPipeline$Builder;"))
	private RenderPipeline.Builder cfi_changeType(RenderPipeline.Builder instance, String name, UniformType type, Operation<RenderPipeline.Builder> original) {
		if (name.equals("cfi_lodMask"))
			return instance.withUniform(name, UniformType.TEXEL_BUFFER, TextureFormat.RED8I);
		else
			return original.call(instance, name, type);
	}
}
