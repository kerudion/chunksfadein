package com.koteinik.chunksfadein.compat.dh.mixin;

import com.koteinik.chunksfadein.Logger;
import com.koteinik.chunksfadein.ShaderUtils;
import com.koteinik.chunksfadein.compat.dh.LodMaskTexture;
import com.koteinik.chunksfadein.compat.dh.ext.DhRenderProgramExt;
import com.koteinik.chunksfadein.compat.dh.ext.LodRendererExt;
import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.SkyFBO;
import com.koteinik.chunksfadein.core.Utils;
import com.koteinik.chunksfadein.hooks.CompatibilityHook;
import com.seibel.distanthorizons.api.interfaces.override.rendering.IDhApiShaderProgram;
import com.seibel.distanthorizons.api.methods.events.sharedParameterObjects.DhApiRenderParam;
import com.seibel.distanthorizons.core.render.RenderBufferHandler;
import com.seibel.distanthorizons.core.render.glObject.texture.DhColorTexture;
import com.seibel.distanthorizons.core.render.renderer.DhTerrainShaderProgram;
import com.seibel.distanthorizons.core.render.renderer.LodRenderer;
import com.seibel.distanthorizons.core.render.renderer.generic.GenericObjectRenderer;
import com.seibel.distanthorizons.core.render.renderer.shaders.SSAOApplyShader;
import com.seibel.distanthorizons.core.render.renderer.shaders.SSAOShader;
import com.seibel.distanthorizons.core.wrapperInterfaces.minecraft.IProfilerWrapper;
import com.seibel.distanthorizons.core.wrapperInterfaces.world.IClientLevelWrapper;
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
public abstract class LodRendererMixin implements LodRendererExt {
	@Shadow
	private IDhApiShaderProgram lodRenderProgram;

	@Shadow
	public static int getActiveColorTextureId() {
		return 0;
	}

	@Shadow
	private boolean usingMcFrameBuffer;

	@Shadow
	private DhColorTexture nullableColorTexture;

	@Override
	public DhRenderProgramExt getShader() {
		if (lodRenderProgram instanceof DhRenderProgramExt ext)
			return ext;
		else
			return null;
	}

	@Override
	public void rebuildShaders() {
		lodRenderProgram.unbind();
		lodRenderProgram.free();
		lodRenderProgram = new DhTerrainShaderProgram();
		SSAOShader.INSTANCE.free();
		SSAOShader.INSTANCE = new SSAOShader();
		SSAOApplyShader.INSTANCE.free();
		SSAOApplyShader.INSTANCE = new SSAOApplyShader();
	}

	@Inject(method = "<init>", at = @At(value = "TAIL"))
	private void modifyConstructor(RenderBufferHandler bufferHandler, GenericObjectRenderer genericObjectRenderer, CallbackInfo ci) {
		ShaderUtils.lodRenderer = this;
	}

	@Inject(method = "setupGLStateAndRenderObjects", at = @At(value = "INVOKE", target = "Lcom/seibel/distanthorizons/api/interfaces/override/rendering/IDhApiFramebuffer;bind()V", shift = At.Shift.AFTER))
	private void modifySetupGLStateAndRenderObjects(IProfilerWrapper profiler, DhApiRenderParam renderEventParam, boolean firstPass, CallbackInfo ci) {
		if (!Config.isModEnabled)
			return;

		LodMaskTexture.createAndUpdate();

		if (!Config.isFadeEnabled || !CompatibilityHook.isDHSSAOEnabled())
			return;

		SkyFBO.bindAttachment(GL30.GL_COLOR_ATTACHMENT1);
	}

	@Inject(
		method = "renderLodPass",
		at = @At(
			value = "INVOKE",
			target = "Lcom/seibel/distanthorizons/core/render/renderer/shaders/DhApplyShader;render(F)V"
		)
	)
	private void modifyRenderLodPass(IClientLevelWrapper clientLevelWrapper, DhApiRenderParam renderEventParam, IProfilerWrapper profiler, boolean runningDeferredPass, CallbackInfo ci) {
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
					getActiveColorTextureId(),
					Utils.mainTargetWidth(),
					Utils.mainTargetHeight(),
					false
				);
			}
		} catch (Exception e) {
			Logger.error("Failed to blit main color texture after DH rendering:", e);
		}
	}
}
