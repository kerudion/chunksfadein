package com.koteinik.chunksfadein.compat.dh.mixin.blaze;

import com.koteinik.chunksfadein.Logger;
import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.SkyFBO;
import com.koteinik.chunksfadein.core.Utils;
import com.koteinik.chunksfadein.hooks.CompatibilityHook;
import com.seibel.distanthorizons.common.render.blaze.BlazeDhMetaRenderer_fabric;
import com.seibel.distanthorizons.core.render.RenderParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BlazeDhMetaRenderer_fabric.class, remap = false)
public class BlazeDhMetaRendererMixin_fabric {
	@Inject(
		method = "applyToMcTexture",
		at = @At(value = "TAIL")
	)
	private void cfi_blitDHBufferAfterDraw(RenderParams renderParams, CallbackInfo ci) {
		if (!Config.isModEnabled || !Config.isFadeEnabled || !CompatibilityHook.isDHRenderingEnabled())
			return;

		SkyFBO fbo = SkyFBO.getInstance();
		if (fbo == null)
			return;

		try {
			fbo.blitFromTexture(Utils.mainColorTexture());
		} catch (Exception e) {
			Logger.error("Failed to blit main color texture after DH rendering:", e);
		}
	}
}
