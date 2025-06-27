package com.koteinik.chunksfadein.mixin.dh;

import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.SkyFBO;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.seibel.distanthorizons.core.render.renderer.shaders.FadeShader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = FadeShader.class, remap = false)
public class FadeShaderMixin {
	@ModifyExpressionValue(method = "onRender", at = @At(value = "INVOKE", target = "Lcom/seibel/distanthorizons/core/render/renderer/LodRenderer;getActiveColorTextureId()I"))
	private int modifyOnRender(int original) {
		if (Config.isFadeEnabled) return original;

		SkyFBO instance = SkyFBO.getInstance();

		if (instance != null)
			return instance.textureId;
		else
			return original;
	}
}
