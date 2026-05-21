package com.koteinik.chunksfadein.compat.dh.mixin.iris;

import com.koteinik.chunksfadein.Logger;
import com.koteinik.chunksfadein.compat.dh.ext.GlDhMetaRendererExt;
import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.SkyFBO;
import com.koteinik.chunksfadein.core.Utils;
import com.koteinik.chunksfadein.hooks.CompatibilityHook;
import com.seibel.distanthorizons.core.render.RenderParams;
import com.seibel.distanthorizons.core.render.renderer.LodRenderer;
import com.seibel.distanthorizons.core.wrapperInterfaces.minecraft.IProfilerWrapper;
import com.seibel.distanthorizons.core.wrapperInterfaces.render.renderPass.IDhMetaRenderer;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.compat.dh.DHCompat;
import net.irisshaders.iris.compat.dh.DHCompatInternal;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import org.lwjgl.opengl.GL30;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LodRenderer.class, remap = false)
public class IrisLodRendererMixin {

	@Shadow
	private IDhMetaRenderer metaRenderer;

	@Inject(
		method = "renderTerrain(Lcom/seibel/distanthorizons/core/render/RenderParams;Lcom/seibel/distanthorizons/core/wrapperInterfaces/minecraft/IProfilerWrapper;Z)V",
		at = @At(
			value = "INVOKE",
			target = "Lcom/seibel/distanthorizons/core/render/renderer/LodRenderer;renderTerrain(Lcom/seibel/distanthorizons/core/wrapperInterfaces/render/renderPass/IDhTerrainRenderer;Lcom/seibel/distanthorizons/core/render/RenderBufferHandler;Lcom/seibel/distanthorizons/core/render/RenderParams;ZLcom/seibel/distanthorizons/core/wrapperInterfaces/minecraft/IProfilerWrapper;)V",
			ordinal = 0
		)
	)
	private void updateMaskAndbindAttachments(RenderParams renderParams, IProfilerWrapper profiler, boolean runningDeferredPass, CallbackInfo ci) {
		if (!Config.isModEnabled || !Config.isFadeEnabled || !CompatibilityHook.isDHSSAOEnabled())
			return;

		SkyFBO.bindAttachment(GL30.GL_COLOR_ATTACHMENT1);
	}

	@Inject(
		method = "renderTerrain(Lcom/seibel/distanthorizons/core/render/RenderParams;Lcom/seibel/distanthorizons/core/wrapperInterfaces/minecraft/IProfilerWrapper;Z)V",
		at = @At(
			value = "INVOKE",
			target = "Lcom/seibel/distanthorizons/core/wrapperInterfaces/render/renderPass/IDhMetaRenderer;runRenderPassCleanup(Lcom/seibel/distanthorizons/core/render/RenderParams;)V",
			shift = At.Shift.AFTER
		)
	)
	private void blitDHBufferAfterDraw(RenderParams renderParams, IProfilerWrapper profiler, boolean runningDeferredPass, CallbackInfo ci) {
		if (!Config.isModEnabled || !Config.isFadeEnabled || !CompatibilityHook.isDHRenderingEnabled())
			return;

		SkyFBO fbo = SkyFBO.getInstance();
		if (fbo == null)
			return;

		try {
			if (CompatibilityHook.isIrisShaderPackInUse()) {
				DHCompatInternal irisDh = (DHCompatInternal) Iris.getPipelineManager()
					.getPipeline()
					.map(WorldRenderingPipeline::getDHCompat)
					.map(DHCompat::getInstance)
					.orElse(null);
				if (irisDh == null) return;

				fbo.blitFromFramebuffer(
					irisDh.getSolidFBWrapper().getId(),
					Utils.mainTargetWidth(),
					Utils.mainTargetHeight(),
					false
				);
			} else {
				fbo.blitFromTexture(
					((GlDhMetaRendererExt) metaRenderer).activeColorTexture(),
					Utils.mainTargetWidth(),
					Utils.mainTargetHeight(),
					true
				);
			}
		} catch (Exception e) {
			Logger.error("Failed to blit main color texture after DH rendering:", e);
		}
	}
}
