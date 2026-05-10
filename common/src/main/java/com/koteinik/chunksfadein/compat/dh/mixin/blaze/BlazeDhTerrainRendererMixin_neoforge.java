package com.koteinik.chunksfadein.compat.dh.mixin.blaze;

import com.koteinik.chunksfadein.Logger;
import com.koteinik.chunksfadein.ShaderUtils;
import com.koteinik.chunksfadein.compat.dh.LodMaskTexture;
import com.koteinik.chunksfadein.compat.dh.ext.BlazeDhTerrainRendererExt;
import com.koteinik.chunksfadein.compat.dh.ext.BlazeLodBufferContainerExt;
import com.koteinik.chunksfadein.compat.dh.ext.GlDeviceExt;
import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.SkyFBO;
import com.koteinik.chunksfadein.core.Utils;
import com.koteinik.chunksfadein.hooks.CompatibilityHook;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.seibel.distanthorizons.api.enums.config.EDhApiMcRenderingFadeMode;
import com.seibel.distanthorizons.common.render.blaze.BlazeDhMetaRenderer_neoforge;
import com.seibel.distanthorizons.common.render.blaze.BlazeDhTerrainRenderer_neoforge;
import com.seibel.distanthorizons.core.dataObjects.render.bufferBuilding.LodBufferContainer;
import com.seibel.distanthorizons.core.render.RenderParams;
import com.seibel.distanthorizons.core.util.RenderUtil;
import com.seibel.distanthorizons.core.util.objects.SortedArraySet;
import com.seibel.distanthorizons.core.wrapperInterfaces.minecraft.IProfilerWrapper;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.koteinik.chunksfadein.compat.dh.DHBlazeState.*;

@Mixin(value = BlazeDhTerrainRenderer_neoforge.class, remap = false)
public abstract class BlazeDhTerrainRendererMixin_neoforge implements BlazeDhTerrainRendererExt {
	@Shadow
	private boolean init;

	@Shadow
	protected abstract void tryInit();

	@Shadow
	private RenderPipeline opaquePipeline;

	@Shadow
	private RenderPipeline transparentPipeline;

	@Override
	public void cfi_rebuildPipeline() {
		if (!init) return;

		init = false;

		GlDeviceExt device = ((GlDeviceExt) RenderSystem.getDevice());

		device.cfi_evictPipeline(opaquePipeline);
		device.cfi_evictPipeline(transparentPipeline);

		tryInit();
	}

	@Inject(method = "<init>", at = @At(value = "TAIL"))
	private void cfi_init(CallbackInfo ci) {
		ShaderUtils.blazeLodRenderer = this;
	}

	@Inject(
		method = "render",
		at = @At(
			value = "INVOKE",
			target = "Lcom/mojang/blaze3d/systems/RenderPass;setUniform(Ljava/lang/String;Lcom/mojang/blaze3d/buffers/GpuBuffer;)V",
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
		if (!Config.isModEnabled || !CompatibilityHook.isDHRenderingEnabled() || !hasAnyProgram())
			return;

		SkyFBO.bind(SKY_LOC);
		LodMaskTexture.bind(LOD_MASK_LOC);

		int prevProgram = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);

		float fadeStartDistanceSq = 0f;
		if (cfi_dhFadeEnabled()) {
			float dhNearClipDistance = RenderUtil.getNearClipPlaneInBlocks();
			dhNearClipDistance += 16f;

			float fadeStartDistance = dhNearClipDistance * 1.5f;

			fadeStartDistanceSq = fadeStartDistance * fadeStartDistance;
		}

		LodMaskTexture texture = LodMaskTexture.getInstance();
		Vec3 cameraPos = texture != null ? Utils.cameraPosition() : null;

		try {
			for (int program : terrainPrograms) {
				if (program == -1) continue;
				GL20.glUseProgram(program);

				if (Config.isFadeEnabled && screenSize != -1)
					GL30.glUniform2f(screenSize, SkyFBO.getWidth(), SkyFBO.getHeight());

				if (cfi_dhFadeEnabled()) {
					if (dhFadeActive != -1) GL30.glUniform1i(dhFadeActive, 1);
					if (dhStartFadeBlockDistanceSq != -1)
						GL30.glUniform1f(dhStartFadeBlockDistanceSq, fadeStartDistanceSq);
				} else {
					if (dhFadeActive != -1) GL30.glUniform1i(dhFadeActive, 0);
					if (dhStartFadeBlockDistanceSq != -1) GL30.glUniform1f(dhStartFadeBlockDistanceSq, 0);
				}

				if (texture != null) {
					if (lodMaskDim != -1)
						GL30.glUniform3f(
							lodMaskDim,
							texture.sizeX, texture.sizeY, texture.sizeZ
						);
					if (lodMaskMaxDist != -1)
						GL30.glUniform3f(
							lodMaskMaxDist,
							(float) texture.sizeX * 8 + 16,
							(float) texture.sizeY * 8 + 16,
							(float) texture.sizeZ * 8 + 16
						);
					if (lodMaskOrigin != -1)
						GL30.glUniform3f(
							lodMaskOrigin,
							(float) Math.floor(cameraPos.x / 16),
							(float) Math.floor(cameraPos.y / 16),
							(float) Math.floor(cameraPos.z / 16)
						);
					if (lodMaskMinY != -1)
						GL30.glUniform1f(
							lodMaskMinY,
							texture.minY
						);
				}
			}
		} finally {
			GL20.glUseProgram(prevProgram);
		}
	}

	@Unique
	private static boolean cfi_dhFadeEnabled() {
		return com.seibel.distanthorizons.core.config.Config.Client.Advanced.Graphics.Quality.vanillaFadeMode.get()
			!= EDhApiMcRenderingFadeMode.NONE;
	}

	@Inject(
		method = "render",
		at = @At(
			value = "INVOKE",
			target = "Lcom/mojang/blaze3d/systems/RenderPass;setUniform(Ljava/lang/String;Lcom/mojang/blaze3d/buffers/GpuBuffer;)V",
			ordinal = 2,
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
		if (!Config.isModEnabled || !CompatibilityHook.isDHRenderingEnabled() || !hasAnyProgram()) return;

		int prevProgram = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);

		try {
			for (int program : terrainPrograms) {
				if (program == -1) continue;
				GL20.glUseProgram(program);

				((BlazeLodBufferContainerExt) bufferContainer).cfi_upload();
			}
		} finally {
			GL20.glUseProgram(prevProgram);
		}
	}

	@Inject(
		method = "render",
		at = @At(value = "TAIL")
	)
	private void cfi_blitDHBufferAfterDraw(
		RenderParams renderEventParam,
		boolean opaquePass,
		SortedArraySet<LodBufferContainer> bufferContainers,
		IProfilerWrapper profiler,
		CallbackInfo ci
	) {
		if (!Config.isModEnabled || !Config.isFadeEnabled || !CompatibilityHook.isDHRenderingEnabled())
			return;

		SkyFBO fbo = SkyFBO.getInstance();
		if (fbo == null)
			return;

		try {
			fbo.blitFromTexture(
				((GlTexture) BlazeDhMetaRenderer_neoforge.INSTANCE.dhColorTextureWrapper.textureView.texture()).glId(),
				Utils.mainTargetWidth(),
				Utils.mainTargetHeight(),
				false
			);
		} catch (Exception e) {
			Logger.error("Failed to blit main color texture after DH rendering:", e);
		}
	}
}
