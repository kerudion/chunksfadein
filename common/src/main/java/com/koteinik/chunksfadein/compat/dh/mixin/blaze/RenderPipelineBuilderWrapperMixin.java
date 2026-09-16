package com.koteinik.chunksfadein.compat.dh.mixin.blaze;

import com.koteinik.chunksfadein.ShaderUtils;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.pipeline.BindGroupLayout;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.pipeline.UniformType;
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

	@WrapOperation(method = "build", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/pipeline/BindGroupLayout$Builder;withUniform(Ljava/lang/String;Lcom/mojang/renderpearl/api/pipeline/UniformType;)Lcom/mojang/renderpearl/api/pipeline/BindGroupLayout$Builder;"))
	private BindGroupLayout.Builder cfi_changeType(BindGroupLayout.Builder instance, String name, UniformType type, Operation<BindGroupLayout.Builder> original) {
		if (name.equals("cfi_lodMask"))
			return instance.withUniform(name, UniformType.TEXEL_BUFFER, GpuFormat.R8_SINT);
		else
			return original.call(instance, name, type);
	}
}
