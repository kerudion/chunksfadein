package com.koteinik.chunksfadein.compat.iris.mixin;

import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.SkyFBO;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.irisshaders.iris.gl.sampler.SamplerHolder;
import net.irisshaders.iris.gl.texture.TextureAccess;
import net.irisshaders.iris.samplers.IrisSamplers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IrisSamplers.class)
public class IrisSamplersMixin {
	@Inject(
		method = "addCustomTextures",
		at = @At("HEAD"),
		remap = false
	)
	private static void onAddCustomTextures(SamplerHolder samplers, Object2ObjectMap<String, TextureAccess> irisCustomTextures, CallbackInfo ci) {
		if (!Config.isModEnabled) return;

		samplers.addDynamicSampler(SkyFBO::getTextureId, "cfi_sky");
	}
}
