package com.koteinik.chunksfadein.compat.dh.mixin.ogl;

import com.koteinik.chunksfadein.compat.dh.LodMaskTexture;
import com.koteinik.chunksfadein.compat.dh.ext.DhRenderProgramExt;
import com.koteinik.chunksfadein.compat.dh.ext.LodBufferContainerExt;
import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.SkyFBO;
import com.koteinik.chunksfadein.core.Utils;
import com.koteinik.chunksfadein.hooks.CompatibilityHook;
import com.llamalad7.mixinextras.sugar.Local;
import com.seibel.distanthorizons.api.enums.config.EDhApiMcRenderingFadeMode;
import com.seibel.distanthorizons.api.methods.events.sharedParameterObjects.DhApiRenderParam;
import com.seibel.distanthorizons.common.render.openGl.glObject.shader.GlShaderProgram;
import com.seibel.distanthorizons.common.render.openGl.terrain.GlDhTerrainShaderProgram_fabric;
import com.seibel.distanthorizons.core.dataObjects.render.bufferBuilding.LodBufferContainer;
import com.seibel.distanthorizons.core.util.RenderUtil;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GlDhTerrainShaderProgram_fabric.class, remap = false)
public abstract class GlDhTerrainShaderProgramMixin_fabric extends GlShaderProgram implements DhRenderProgramExt {
	@Shadow
	public int uClipDistance;
	@Unique
	private int screenSize;
	@Unique
	private int chunkFadeData;
	@Unique
	private int lodMask;
	@Unique
	private int lodMaskDim;
	@Unique
	private int lodMaskMaxDist;
	@Unique
	private int lodMaskOrigin;
	@Unique
	private int lodMaskMinY;
	@Unique
	private int terrainFadeTexture;
	@Unique
	private int dhFadeActive;
	@Unique
	private int dhStartFadeBlockDistanceSq;

	public GlDhTerrainShaderProgramMixin_fabric(String vertResourcePath, String fragResourcePath, String attribute) {
		super(vertResourcePath, fragResourcePath, attribute);
	}

	@Inject(method = "<init>", at = @At("RETURN"))
	private void modifyConstructor(CallbackInfo ci) {
		this.screenSize = tryGetUniformLocation("cfi_screenSize");
		this.chunkFadeData = tryGetUniformLocation("cfi_chunkFadeData");
		this.lodMask = tryGetUniformLocation("cfi_lodMask");
		this.lodMaskDim = tryGetUniformLocation("cfi_lodMaskDim");
		this.lodMaskMaxDist = tryGetUniformLocation("cfi_lodMaskMaxDist");
		this.lodMaskOrigin = tryGetUniformLocation("cfi_lodMaskOrigin");
		this.lodMaskMinY = tryGetUniformLocation("cfi_lodMaskMinY");
		this.terrainFadeTexture = tryGetUniformLocation("cfi_sky");
		this.dhFadeActive = tryGetUniformLocation("cfi_dhFadeActive");
		this.dhStartFadeBlockDistanceSq = tryGetUniformLocation("cfi_dhStartFadeBlockDistanceSq");
	}

	@Override
	public void bindUniforms(float x, float y, float z, float w) {
		if (chunkFadeData != -1)
			GL30.glUniform4f(chunkFadeData, x, y, z, w);
	}

	@Inject(method = "bind", at = @At(value = "TAIL"))
	private void modifyBind(CallbackInfo ci) {
		if (!Config.isModEnabled)
			return;

		if (lodMask != -1)
			LodMaskTexture.bind(14);

		if (!Config.isFadeEnabled)
			return;

		if (terrainFadeTexture != -1)
			SkyFBO.bind(15);
	}

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/seibel/distanthorizons/coreapi/DependencyInjection/ApiEventInjector;fireAllEvents(Ljava/lang/Class;Ljava/lang/Object;)Z"))
	private void modifyRender(CallbackInfo ci, @Local(name = "bufferContainer") LodBufferContainer bufferContainer) {
		if (!Config.isModEnabled || !CompatibilityHook.isDHRenderingEnabled())
			return;

		((LodBufferContainerExt) bufferContainer)
			.bind(this);
	}

	@Inject(method = "fillUniformData", at = @At(value = "TAIL"))
	private void modifyFillUniformData(DhApiRenderParam renderParameters, CallbackInfo ci) {
		if (!Config.isModEnabled)
			return;

		if (Config.isFadeEnabled) {
			if (screenSize != -1) GL30.glUniform2f(screenSize, SkyFBO.getWidth(), SkyFBO.getHeight());
			if (terrainFadeTexture != -1) GL30.glUniform1i(terrainFadeTexture, 15);
		}

		if (dhFadeEnabled()) {
			if (dhFadeActive != -1) GL30.glUniform1i(dhFadeActive, 1);

			float dhNearClipDistance = RenderUtil.getNearClipPlaneInBlocks();
			dhNearClipDistance += 16f;

			float fadeStartDistance = dhNearClipDistance * 1.5f;

			if (dhStartFadeBlockDistanceSq != -1)
				GL30.glUniform1f(dhStartFadeBlockDistanceSq, fadeStartDistance * fadeStartDistance);
		} else {
			if (dhFadeActive != -1) GL30.glUniform1i(dhFadeActive, 0);
			if (dhStartFadeBlockDistanceSq != -1) GL30.glUniform1f(dhStartFadeBlockDistanceSq, 0);
		}

		if (lodMask != -1)
			GL30.glUniform1i(lodMask, 14);

		LodMaskTexture texture = LodMaskTexture.getInstance();
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
			if (lodMaskOrigin != -1) {
				Vec3 cameraPos = Utils.cameraPosition();
				GL30.glUniform3f(
					lodMaskOrigin,
					(float) Math.floor(cameraPos.x / 16),
					(float) Math.floor(cameraPos.y / 16),
					(float) Math.floor(cameraPos.z / 16)
				);
			}
			if (lodMaskMinY != -1)
				GL30.glUniform1f(
					lodMaskMinY,
					texture.minY
				);
		}
	}

	@Inject(method = "unbind", at = @At(value = "TAIL"))
	private void modifyUnbind(CallbackInfo ci) {
		if (!Config.isModEnabled)
			return;

		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		if (Config.isFadeEnabled)
			GL13.glBindTexture(GL13.GL_TEXTURE_2D, 0);
		GL13.glBindTexture(GL13.GL_TEXTURE_3D, 0);
	}

	private static boolean dhFadeEnabled() {
		return com.seibel.distanthorizons.core.config.Config.Client.Advanced.Graphics.Quality.vanillaFadeMode.get()
			!= EDhApiMcRenderingFadeMode.NONE;
	}
}
