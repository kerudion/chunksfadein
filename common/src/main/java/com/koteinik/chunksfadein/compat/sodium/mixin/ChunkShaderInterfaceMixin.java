package com.koteinik.chunksfadein.compat.sodium.mixin;

import com.koteinik.chunksfadein.Logger;
import com.koteinik.chunksfadein.compat.sodium.ext.ChunkShaderInterfaceExt;
import com.koteinik.chunksfadein.core.FadeShaderInterface;
import com.koteinik.chunksfadein.core.SkyFBO;
import com.koteinik.chunksfadein.hooks.CompatibilityHook;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.caffeinemc.mods.sodium.client.gl.buffer.GlMutableBuffer;
import net.caffeinemc.mods.sodium.client.gl.shader.uniform.*;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ChunkShaderOptions;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.DefaultShaderInterface;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ShaderBindingContext;
import org.lwjgl.opengl.GL13;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.IntFunction;

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

	@Redirect(
		method = "<init>",
		at = @At(
			value = "INVOKE",
			target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/shader/ShaderBindingContext;bindUniform(Ljava/lang/String;Ljava/util/function/IntFunction;)Lnet/caffeinemc/mods/sodium/client/gl/shader/uniform/GlUniform;"
		)
	)
	private GlUniform<?> redirectBinds(
		ShaderBindingContext instance,
		String s,
		IntFunction<? extends GlUniform<?>> intFunction
	) {
		return instance.bindUniformOptional(s, intFunction);
	}

	@WrapWithCondition(
		method = "setupState",
		at = @At(
			value = "INVOKE",
			target = "Lnet/caffeinemc/mods/sodium/client/gl/shader/uniform/GlUniformFloat;setFloat(F)V"
		)
	)
	private boolean wrapSet(GlUniformFloat instance, float value) {
		return instance != null;
	}

	@WrapWithCondition(
		method = "setChunkData",
		at = @At(
			value = "INVOKE",
			target = "Lnet/caffeinemc/mods/sodium/client/gl/shader/uniform/GlUniformInt;set(Ljava/lang/Integer;)V"
		)
	)
	private boolean wrapSet(GlUniformInt instance, Integer value) {
		return instance != null;
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
			int prevActive = GL13.glGetInteger(GL13.GL_ACTIVE_TEXTURE);

			SkyFBO.bind(13);
			sky.set(13);

			GL13.glActiveTexture(prevActive);
		}
	}
}
