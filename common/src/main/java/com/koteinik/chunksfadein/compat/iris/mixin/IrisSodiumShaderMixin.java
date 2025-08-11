package com.koteinik.chunksfadein.compat.iris.mixin;

import java.util.List;
import java.util.function.Supplier;

import com.koteinik.chunksfadein.core.SkyFBO;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.common.collect.ImmutableSet;
import com.koteinik.chunksfadein.core.FadeShaderInterface;
import com.koteinik.chunksfadein.compat.sodium.ext.ChunkShaderInterfaceExt;
import com.llamalad7.mixinextras.sugar.Local;

import net.caffeinemc.mods.sodium.client.gl.buffer.GlMutableBuffer;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ShaderBindingContext;
import net.irisshaders.iris.gl.blending.BlendModeOverride;
import net.irisshaders.iris.gl.blending.BufferBlendOverride;
import net.irisshaders.iris.gl.program.ProgramSamplers;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.programs.SodiumPrograms.Pass;
import net.irisshaders.iris.pipeline.programs.SodiumShader;
import net.irisshaders.iris.uniforms.custom.CustomUniforms;

@Mixin(value = SodiumShader.class, remap = false)
public class IrisSodiumShaderMixin implements ChunkShaderInterfaceExt {
	private FadeShaderInterface fadeInterface;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void modifyConstructor(IrisRenderingPipeline pipeline, Pass pass, ShaderBindingContext context, int handle, BlendModeOverride blendModeOverride,
	                               List<BufferBlendOverride> bufferBlendOverrides, CustomUniforms customUniforms, Supplier<ImmutableSet<Integer>> flipState, float alphaTest, boolean containsTessellation, CallbackInfo ci) {
		fadeInterface = new FadeShaderInterface(context);
	}

	@Inject(method = "buildSamplers", at = @At(value = "INVOKE", target = "Lnet/irisshaders/iris/gl/program/ProgramSamplers$Builder;build()Lnet/irisshaders/iris/gl/program/ProgramSamplers;"))
	private void modifyBuildSamplers(IrisRenderingPipeline pipeline, Pass pass, int handle, boolean isShadowPass, Supplier<ImmutableSet<Integer>> flipState,
	                                 CallbackInfoReturnable<ProgramSamplers> cir,
	                                 @Local ProgramSamplers.Builder builder) {
		builder.addDynamicSampler(SkyFBO::getTextureId, "cfi_sky");
	}

	@Override
	public void bindUniforms(GlMutableBuffer fadeDataBuffer) {
		fadeInterface.bindUniforms(fadeDataBuffer);
	}
}
