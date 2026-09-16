package com.koteinik.chunksfadein.compat.iris.mixin;

import com.koteinik.chunksfadein.core.SkyFBO;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import net.irisshaders.iris.gl.blending.AlphaTest;
import net.irisshaders.iris.gl.blending.BlendModeOverride;
import net.irisshaders.iris.gl.framebuffer.GlFramebuffer;
import net.irisshaders.iris.gl.program.ProgramSamplers;
import net.irisshaders.iris.gl.sampler.GlSampler;
import net.irisshaders.iris.gl.texture.TextureType;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.programs.ExtendedShader;
import net.irisshaders.iris.pipeline.programs.ShaderKey;
import net.irisshaders.iris.pipeline.transform.Patch;
import net.irisshaders.iris.uniforms.custom.CustomUniforms;
import org.lwjgl.opengl.GL31;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

@Mixin(value = ExtendedShader.class, remap = false)
public class ExtendedShaderMixin {
	@Inject(method = "<init>", at = @At(value = "RETURN"))
	private void modifyInit(int programId, String string, VertexFormat vertexFormat, boolean usesTessellation, GlFramebuffer writingToBeforeTranslucent, GlFramebuffer writingToAfterTranslucent, BlendModeOverride blendModeOverride, AlphaTest alphaTest, Consumer uniformCreator, BiConsumer samplerCreator, ShaderKey shaderKey, IrisRenderingPipeline parent, List bufferBlendOverrides, CustomUniforms customUniforms, Patch patch, CallbackInfo ci) {
		int block = GL31.glGetUniformBlockIndex(programId, "cfi_u_Globals");
		if (block != GL31.GL_INVALID_INDEX)
			GL31.glUniformBlockBinding(programId, block, 7);
	}

	@WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/irisshaders/iris/gl/program/ProgramSamplers;builder(ILjava/util/Set;)Lnet/irisshaders/iris/gl/program/ProgramSamplers$Builder;"))
	private ProgramSamplers.Builder modifyInit(int program, Set<Integer> reservedTextureUnits, Operation<ProgramSamplers.Builder> original) {
		reservedTextureUnits = new HashSet<>(reservedTextureUnits);
		reservedTextureUnits.add(13);
		return original.call(program, reservedTextureUnits);
	}

	@WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/irisshaders/iris/gl/program/ProgramSamplers$Builder;addDynamicSampler(Lnet/irisshaders/iris/gl/texture/TextureType;Ljava/util/function/IntSupplier;Ljava/util/function/Supplier;[Ljava/lang/String;)Z"))
	private boolean modifyInit(ProgramSamplers.Builder instance, TextureType type, IntSupplier texture, Supplier<GlSampler> sampler, String[] names, Operation<Boolean> original) {
		instance.addDynamicSampler(TextureType.TEXTURE_2D, () -> SkyFBO.getGlInstance().textureId(), null, "cfi_sky");
		instance.addExternalSampler(13, "cfi_u_FadeData");

		return original.call(instance, type, texture, sampler, names);
	}
}
