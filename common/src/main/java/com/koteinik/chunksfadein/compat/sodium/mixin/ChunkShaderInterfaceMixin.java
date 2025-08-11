package com.koteinik.chunksfadein.compat.sodium.mixin;

import com.koteinik.chunksfadein.Logger;
import com.koteinik.chunksfadein.core.FadeShaderInterface;
import com.koteinik.chunksfadein.core.SkyFBO;
import com.koteinik.chunksfadein.compat.sodium.ext.ChunkShaderInterfaceExt;
import com.koteinik.chunksfadein.hooks.CompatibilityHook;
import net.caffeinemc.mods.sodium.client.gl.buffer.GlMutableBuffer;
import net.caffeinemc.mods.sodium.client.gl.shader.uniform.GlUniformInt;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ChunkShaderOptions;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.DefaultShaderInterface;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ShaderBindingContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = DefaultShaderInterface.class, remap = false)
public abstract class ChunkShaderInterfaceMixin implements ChunkShaderInterfaceExt {
	private GlUniformInt sky;

	private FadeShaderInterface fadeInterface;
	private static boolean warned = false;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void modifyConstructor(ShaderBindingContext context, ChunkShaderOptions options, CallbackInfo ci) {
		if (CompatibilityHook.isIrisShaderPackInUse())
			return;

		fadeInterface = new FadeShaderInterface(context);
		sky = context.bindUniformOptional("cfi_sky", GlUniformInt::new);
	}

	@Override
	public void bindUniforms(GlMutableBuffer fadeDataBuffer) {
		if (fadeInterface == null) {
			if (CompatibilityHook.isIrisShaderPackInUse() && !warned) {
				Logger.warn("Shader pack is in use, but Sodium's shader interface is used. Something went really wrong!");
				warned = true;
			}
			return;
		}

		fadeInterface.bindUniforms(fadeDataBuffer);

		if (sky != null) {
			SkyFBO.active(13);
			sky.set(13);
		}
	}
}
