package com.koteinik.chunksfadein.compat.monocle.mixin;

import com.koteinik.chunksfadein.compat.sodium.ext.ChunkShaderInterfaceExt;
import com.koteinik.chunksfadein.compat.sodium.ext.GlMutableBufferExt;
import com.koteinik.chunksfadein.compat.sodium.ext.ShaderBindingContextExt;
import com.koteinik.chunksfadein.core.FadeShaderInterface;
import com.koteinik.chunksfadein.core.SkyFBO;
import com.llamalad7.mixinextras.sugar.Local;
import net.irisshaders.iris.gl.program.ProgramSamplers;
import org.embeddedt.embeddium.impl.render.chunk.shader.ShaderBindingContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "dev.ferriarnus.monocle.irisCompatibility.impl.EmbeddiumShader", remap = false)
public class EmbeddiumShaderMixin implements ChunkShaderInterfaceExt {
	private FadeShaderInterface fadeInterface;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void modifyConstructor(CallbackInfo ci, @Local(argsOnly = true) ShaderBindingContext context) {
		fadeInterface = new FadeShaderInterface((ShaderBindingContextExt) context);
	}

	@Inject(method = "buildSamplers", at = @At(value = "INVOKE", target = "Lnet/irisshaders/iris/gl/program/ProgramSamplers$Builder;build()Lnet/irisshaders/iris/gl/program/ProgramSamplers;"))
	private void modifyBuildSamplers(CallbackInfoReturnable<ProgramSamplers> cir, @Local ProgramSamplers.Builder builder) {
		builder.addDynamicSampler(SkyFBO::getTextureId, "cfi_sky");
	}

	@Override
	public void bindUniforms(GlMutableBufferExt fadeDataBuffer) {
		fadeInterface.bindUniforms(fadeDataBuffer);
	}
}
