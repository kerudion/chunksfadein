package com.koteinik.chunksfadein.compat.dh.mixin.iris;

import com.koteinik.chunksfadein.Logger;
import com.koteinik.chunksfadein.ShaderUtils;
import com.koteinik.chunksfadein.compat.dh.LodMaskTexture;
import com.koteinik.chunksfadein.compat.dh.ext.DhRenderProgramExt;
import com.koteinik.chunksfadein.compat.dh.ext.LodBufferContainerExt;
import com.koteinik.chunksfadein.compat.dh.ext.LodRendererExt;
import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.SkyFBO;
import com.koteinik.chunksfadein.core.Utils;
import com.koteinik.chunksfadein.hooks.CompatibilityHook;
import com.llamalad7.mixinextras.sugar.Local;
import com.seibel.distanthorizons.api.interfaces.override.rendering.IDhApiShaderProgram;
import com.seibel.distanthorizons.api.methods.events.sharedParameterObjects.DhApiRenderParam;
import com.seibel.distanthorizons.core.dataObjects.render.bufferBuilding.LodBufferContainer;
import com.seibel.distanthorizons.core.render.RenderBufferHandler;
import com.seibel.distanthorizons.core.render.renderer.DhTerrainShaderProgram;
import com.seibel.distanthorizons.core.render.renderer.LodRenderer;
import com.seibel.distanthorizons.core.render.renderer.RenderParams;
import com.seibel.distanthorizons.core.render.renderer.shaders.SSAOApplyShader;
import com.seibel.distanthorizons.core.render.renderer.shaders.SSAOShader;
import com.seibel.distanthorizons.core.wrapperInterfaces.minecraft.IProfilerWrapper;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.compat.dh.DHCompat;
import net.irisshaders.iris.compat.dh.DHCompatInternal;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.IntBuffer;

@Mixin(value = LodRenderer.class, remap = false)
public abstract class IrisLodRendererMixin implements LodRendererExt {
	@Shadow
	private IDhApiShaderProgram lodRenderProgram;

	@Shadow
	public int getActiveColorTextureId() {
		return 0;
	}

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
	private void modifyConstructor(CallbackInfo ci) {
		ShaderUtils.lodRenderer = this;
	}

	@Inject(
		method = "setGLState",
		at = @At(
			value = "INVOKE",
			target = "Lorg/lwjgl/opengl/GL32;glClearDepth(D)V"
		)
	)
	private void avoidClear(DhApiRenderParam renderEventParam, boolean firstPass, CallbackInfo ci) {
		if (!Config.isModEnabled)
			return;

		LodMaskTexture.createAndUpdate();

		if (!Config.isFadeEnabled || !CompatibilityHook.isDHSSAOEnabled())
			return;

		try (MemoryStack stack = MemoryStack.stackPush()) {
			IntBuffer drawBuffers = stack.mallocInt(1);
			drawBuffers.put(GL30.GL_COLOR_ATTACHMENT0);
			drawBuffers.flip();
			GL20.glDrawBuffers(drawBuffers);
		}
	}

	@Inject(
		method = "renderLodPass(Lcom/seibel/distanthorizons/core/render/renderer/RenderParams;Lcom/seibel/distanthorizons/core/wrapperInterfaces/minecraft/IProfilerWrapper;Z)V",
		at = @At(
			value = "INVOKE",
			target = "Lcom/seibel/distanthorizons/core/render/renderer/LodRenderer;renderLodPass(Lcom/seibel/distanthorizons/api/interfaces/override/rendering/IDhApiShaderProgram;Lcom/seibel/distanthorizons/core/render/RenderBufferHandler;Lcom/seibel/distanthorizons/core/render/renderer/RenderParams;Z)V",
			ordinal = 0
		)
	)
	private void updateMaskAndbindAttachments(RenderParams renderParams, IProfilerWrapper profiler, boolean runningDeferredPass, CallbackInfo ci) {
		if (!Config.isModEnabled || !Config.isFadeEnabled || !CompatibilityHook.isDHSSAOEnabled())
			return;

		SkyFBO.bindAttachment(GL30.GL_COLOR_ATTACHMENT1);
	}

	@Inject(
		method = "renderLodPass(Lcom/seibel/distanthorizons/api/interfaces/override/rendering/IDhApiShaderProgram;Lcom/seibel/distanthorizons/core/render/RenderBufferHandler;Lcom/seibel/distanthorizons/core/render/renderer/RenderParams;Z)V",
		at = @At(
			value = "INVOKE",
			target = "Lcom/seibel/distanthorizons/core/render/renderer/LodRenderer;setShaderProgramMvmOffset(Lcom/seibel/distanthorizons/core/pos/blockPos/DhBlockPos;Lcom/seibel/distanthorizons/api/interfaces/override/rendering/IDhApiShaderProgram;Lcom/seibel/distanthorizons/core/render/renderer/RenderParams;)V",
			shift = At.Shift.AFTER
		)
	)
	private void updateUniforms(
		IDhApiShaderProgram shaderProgram,
		RenderBufferHandler lodBufferHandler,
		RenderParams renderEventParam,
		boolean opaquePass,
		CallbackInfo ci,
		@Local LodBufferContainer bufferContainer
	) {
		if (!Config.isModEnabled || !CompatibilityHook.isDHRenderingEnabled())
			return;

		((LodBufferContainerExt) bufferContainer)
			.bind((LodRenderer) (Object) this);
	}

	@Inject(
		method = "renderLodPass(Lcom/seibel/distanthorizons/core/render/renderer/RenderParams;Lcom/seibel/distanthorizons/core/wrapperInterfaces/minecraft/IProfilerWrapper;Z)V",
		at = @At(
			value = "INVOKE",
			target = "Lcom/seibel/distanthorizons/api/interfaces/override/rendering/IDhApiShaderProgram;unbind()V",
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
					getActiveColorTextureId(),
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
