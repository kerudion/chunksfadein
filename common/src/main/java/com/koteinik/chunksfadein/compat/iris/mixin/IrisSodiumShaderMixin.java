package com.koteinik.chunksfadein.compat.iris.mixin;

import com.koteinik.chunksfadein.compat.sodium.ext.ChunkShaderInterfaceExt;
import com.koteinik.chunksfadein.compat.sodium.ext.GlMutableBufferExt;
import com.koteinik.chunksfadein.compat.sodium.ext.ShaderBindingContextExt;
import com.koteinik.chunksfadein.core.FadeShaderInterface;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderOptions;
import net.irisshaders.iris.compat.sodium.impl.shader_overrides.IrisChunkShaderInterface;
import net.irisshaders.iris.gl.blending.BlendModeOverride;
import net.irisshaders.iris.pipeline.SodiumTerrainPipeline;
import net.irisshaders.iris.uniforms.custom.CustomUniforms;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = IrisChunkShaderInterface.class, remap = false)
public class IrisSodiumShaderMixin implements ChunkShaderInterfaceExt {
	private FadeShaderInterface fadeInterface;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void modifyConstructor(int handle, net.irisshaders.iris.compat.sodium.impl.shader_overrides.ShaderBindingContextExt context, SodiumTerrainPipeline pipeline, ChunkShaderOptions options, boolean isTess, boolean isShadowPass, BlendModeOverride blendModeOverride, List bufferOverrides, float alpha, CustomUniforms customUniforms, CallbackInfo ci) {
		fadeInterface = new FadeShaderInterface((ShaderBindingContextExt) context);
	}

	@Override
	public void bindUniforms(GlMutableBufferExt fadeDataBuffer) {
		fadeInterface.bindUniforms(fadeDataBuffer);
	}
}
