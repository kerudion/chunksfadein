package com.koteinik.chunksfadein.compat.dh.mixin;

import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.SkyFBO;
import com.koteinik.chunksfadein.hooks.CompatibilityHook;
import com.seibel.distanthorizons.core.render.renderer.shaders.AbstractShaderRenderer;
import com.seibel.distanthorizons.core.render.renderer.shaders.SSAOApplyShader;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SSAOApplyShader.class, remap = false)
public abstract class SSAOApplyShaderMixin extends AbstractShaderRenderer {
	@Unique
	private int fadeTex;

	@Inject(method = "onInit", at = @At(value = "TAIL"))
	private void modifyOnInit(CallbackInfo ci) {
		fadeTex = shader.tryGetUniformLocation("cfi_fadeTex");
	}

	@Inject(method = "onApplyUniforms", at = @At(value = "TAIL"))
	private void modifyOnApplyUniforms(float a, CallbackInfo ci) {
		if (!Config.isModEnabled || !Config.isFadeEnabled || !CompatibilityHook.isDHSSAOEnabled())
			return;

		if (fadeTex != -1)
			GL20.glUniform1i(fadeTex, 13);
	}

	@Inject(method = "onRender", at = @At(value = "HEAD"))
	private void modifyOnRenderHead(CallbackInfo ci) {
		if (!Config.isModEnabled || !Config.isFadeEnabled)
			return;

		GL13.glActiveTexture(GL13.GL_TEXTURE13);
		GL13.glBindTexture(GL13.GL_TEXTURE_2D, SkyFBO.getTextureId());
	}

	@Inject(method = "onRender", at = @At(value = "INVOKE", target = "Lcom/seibel/distanthorizons/core/render/renderer/ScreenQuad;render()V"))
	private void modifyOnRender(CallbackInfo ci) {
		if (!Config.isModEnabled || !Config.isFadeEnabled)
			return;

		GL32.glBlendFuncSeparate(GL32.GL_SRC_ALPHA, GL32.GL_ONE_MINUS_SRC_ALPHA, GL32.GL_ZERO, GL32.GL_ONE);
	}
}
