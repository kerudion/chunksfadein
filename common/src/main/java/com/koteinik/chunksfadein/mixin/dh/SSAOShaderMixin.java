package com.koteinik.chunksfadein.mixin.dh;

import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.SkyFBO;
import com.koteinik.chunksfadein.hooks.CompatibilityHook;
import com.seibel.distanthorizons.core.render.renderer.LodRenderer;
import com.seibel.distanthorizons.core.render.renderer.shaders.AbstractShaderRenderer;
import com.seibel.distanthorizons.core.render.renderer.shaders.SSAOShader;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SSAOShader.class, remap = false)
public abstract class SSAOShaderMixin extends AbstractShaderRenderer {
	@Unique
	private int fadeTexture;
	@Unique
	private int dhColorTexture;

	@Inject(method = "onInit", at = @At(value = "TAIL"))
	private void modifyOnInit(CallbackInfo ci) {
		fadeTexture = shader.tryGetUniformLocation("cfi_fadeTex");
		dhColorTexture = shader.tryGetUniformLocation("cfi_dhColorTex");
	}

	@Inject(method = "onApplyUniforms", at = @At(value = "TAIL"))
	private void modifyOnApplyUniforms(float a, CallbackInfo ci) {
		if (!Config.isModEnabled || !Config.isFadeEnabled || !CompatibilityHook.isDHSSAOEnabled())
			return;

		if (fadeTexture != -1)
			GL20.glUniform1i(fadeTexture, 13);
		if (dhColorTexture != -1)
			GL20.glUniform1i(dhColorTexture, 3);
	}

	@Inject(method = "onRender", at = @At(value = "HEAD"))
	private void modifyOnRenderHead(CallbackInfo ci) {
		if (!Config.isModEnabled || !Config.isFadeEnabled)
			return;

		SkyFBO.active(13);

		GL13.glActiveTexture(GL13.GL_TEXTURE3);
		GL13.glBindTexture(GL13.GL_TEXTURE_2D, LodRenderer.getActiveColorTextureId());
	}
}
