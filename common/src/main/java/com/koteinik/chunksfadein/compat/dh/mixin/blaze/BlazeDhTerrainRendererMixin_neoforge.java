package com.koteinik.chunksfadein.compat.dh.mixin.blaze;

import com.koteinik.chunksfadein.ShaderUtils;
import com.koteinik.chunksfadein.compat.dh.DHBlazeUniforms;
import com.koteinik.chunksfadein.compat.dh.LodMaskTexture;
import com.koteinik.chunksfadein.compat.dh.ext.BlazeDhTerrainRendererExt;
import com.koteinik.chunksfadein.compat.dh.ext.BlazeLodBufferContainerExt;
import com.koteinik.chunksfadein.compat.dh.ext.RenderPassWrapperExt;
import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.SkyFBO;
import com.koteinik.chunksfadein.hooks.CompatibilityHook;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.seibel.distanthorizons.common.render.blaze.BlazeDhTerrainRenderer_neoforge;
import com.seibel.distanthorizons.common.render.blaze.wrappers.RenderPassWrapper_neoforge;
import com.seibel.distanthorizons.common.render.blaze.wrappers.RenderPipelineBuilderWrapper_neoforge;
import com.seibel.distanthorizons.core.dataObjects.render.bufferBuilding.LodBufferContainer;
import com.seibel.distanthorizons.core.render.RenderParams;
import com.seibel.distanthorizons.core.util.objects.SortedArraySet;
import com.seibel.distanthorizons.core.wrapperInterfaces.minecraft.IProfilerWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BlazeDhTerrainRenderer_neoforge.class, remap = false)
public abstract class BlazeDhTerrainRendererMixin_neoforge implements BlazeDhTerrainRendererExt {
	@Shadow
	private boolean init;

	@Shadow
	protected abstract void tryInit();

	@Override
	public void cfi_rebuildPipeline() {
		if (!init) return;

		init = false;
		tryInit();
	}

	@Inject(method = "<init>", at = @At(value = "TAIL"))
	private void cfi_init(CallbackInfo ci) {
		ShaderUtils.blazeLodRenderer = this;
	}

	@Inject(method = "tryInit", at = @At(value = "INVOKE", target = "Lcom/seibel/distanthorizons/common/render/blaze/wrappers/RenderPipelineBuilderWrapper_neoforge;withVertexMode(Lcom/seibel/distanthorizons/common/render/blaze/wrappers/RenderPipelineBuilderWrapper$EDhVertexMode_neoforge;)Lcom/seibel/distanthorizons/common/render/blaze/wrappers/RenderPipelineBuilderWrapper_neoforge;"))
	private void cfi_injectStuffIntoPipeline(CallbackInfo ci, @Local(name = "pipelineBuilder") RenderPipelineBuilderWrapper_neoforge pipelineBuilder) {
		pipelineBuilder.withUniformBuffer("cfi_u_DHPerDraw");
		pipelineBuilder.withUniformBuffer("cfi_u_DHGlobals");
		pipelineBuilder.withSampler("cfi_sky");
		pipelineBuilder.withUniformBuffer("cfi_lodMask");
	}

	@Inject(
		method = "render",
		at = @At(
			value = "INVOKE",
			target = "Lcom/seibel/distanthorizons/common/render/blaze/wrappers/uniform/BlazeUniformBufferWrapper;finishAndUpload()V",
			ordinal = 1,
			shift = At.Shift.AFTER
		)
	)
	private void cfi_setup(
		RenderParams renderEventParam,
		boolean opaquePass,
		SortedArraySet<LodBufferContainer> bufferContainers,
		IProfilerWrapper profiler,
		CallbackInfo ci
	) {
		if (!Config.isModEnabled || !CompatibilityHook.isDHRenderingEnabled())
			return;

		DHBlazeUniforms.setUniforms();
	}

	@Inject(
		method = "render",
		at = @At(
			value = "INVOKE",
			target = "Lcom/seibel/distanthorizons/common/render/blaze/wrappers/RenderPassWrapper_neoforge;setUniform(Ljava/lang/String;Lcom/seibel/distanthorizons/common/render/blaze/wrappers/uniform/BlazeUniformBufferWrapper;)V",
			ordinal = 1,
			shift = At.Shift.AFTER
		)
	)
	private void cfi_setup(
		RenderParams renderEventParam,
		boolean opaquePass,
		SortedArraySet<LodBufferContainer> bufferContainers,
		IProfilerWrapper profiler,
		CallbackInfo ci,
		@Local(name = "renderPassWrapper") RenderPassWrapper_neoforge renderPassWrapper
	) {
		if (!Config.isModEnabled || !CompatibilityHook.isDHRenderingEnabled())
			return;

		RenderPass pass = ((RenderPassWrapperExt) renderPassWrapper).cfi_getPass();

		pass.setUniform("cfi_u_DHGlobals", DHBlazeUniforms.buffer);
		pass.setUniform("cfi_lodMask", LodMaskTexture.getInstance().getBuffer());
		pass.bindTexture(
			"cfi_sky",
			SkyFBO.getInstance().texture.getColorTextureView(),
			RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR)
		);
	}

	@Inject(
		method = "render",
		at = @At(
			value = "INVOKE",
			target = "Lcom/seibel/distanthorizons/common/render/blaze/wrappers/RenderPassWrapper_neoforge;setUniform(Ljava/lang/String;Lcom/seibel/distanthorizons/common/render/blaze/wrappers/uniform/BlazeUniformBufferWrapper;)V",
			ordinal = 2,
			shift = At.Shift.AFTER
		)
	)
	private void cfi_bindPerDraw(
		RenderParams renderEventParam,
		boolean opaquePass,
		SortedArraySet<LodBufferContainer> bufferContainers,
		IProfilerWrapper profiler,
		CallbackInfo ci,
		@Local(name = "renderPassWrapper") RenderPassWrapper_neoforge renderPassWrapper,
		@Local(name = "bufferContainer") LodBufferContainer bufferContainer
	) {
		if (!Config.isModEnabled || !CompatibilityHook.isDHRenderingEnabled())
			return;

		GpuBuffer fade = ((BlazeLodBufferContainerExt) bufferContainer).cfi_getBuffer();
		if (fade == null)
			return;

		((RenderPassWrapperExt) renderPassWrapper).cfi_getPass()
			.setUniform("cfi_u_DHPerDraw", fade);
	}

	@Inject(
		method = "render",
		at = @At(
			value = "INVOKE",
			target = "Lcom/seibel/distanthorizons/core/wrapperInterfaces/render/objects/ILodContainerUniformBufferWrapper;tryUpload(Lcom/seibel/distanthorizons/core/dataObjects/render/bufferBuilding/LodBufferContainer;)V",
			shift = At.Shift.AFTER
		)
	)
	private void cfi_uploadUniforms(
		RenderParams renderEventParam,
		boolean opaquePass,
		SortedArraySet<LodBufferContainer> bufferContainers,
		IProfilerWrapper profiler,
		CallbackInfo ci,
		@Local(name = "bufferContainer") LodBufferContainer bufferContainer
	) {
		if (!Config.isModEnabled || !CompatibilityHook.isDHRenderingEnabled()) return;

		((BlazeLodBufferContainerExt) bufferContainer).cfi_upload();
	}
}
