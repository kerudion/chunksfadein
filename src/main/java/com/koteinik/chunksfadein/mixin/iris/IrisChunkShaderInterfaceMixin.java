package com.koteinik.chunksfadein.mixin.iris;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import net.caffeinemc.mods.sodium.client.render.chunk.shader.ShaderBindingContext;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.programs.SodiumPrograms;
import net.irisshaders.iris.pipeline.programs.SodiumShader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.FadeShaderInterface;
import com.koteinik.chunksfadein.extensions.ChunkShaderInterfaceExt;

import net.caffeinemc.mods.sodium.client.gl.buffer.GlMutableBuffer;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ChunkShaderOptions;
import net.irisshaders.iris.gl.blending.BlendModeOverride;
import net.irisshaders.iris.gl.blending.BufferBlendOverride;
import net.irisshaders.iris.uniforms.custom.CustomUniforms;

@Pseudo
@Mixin(value = SodiumShader.class, remap = false)
public class IrisChunkShaderInterfaceMixin implements ChunkShaderInterfaceExt {
    private FadeShaderInterface fadeInterface;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void modifyConstructor(IrisRenderingPipeline pipeline, SodiumPrograms.Pass pass, ShaderBindingContext context, int handle, Optional blendModeOverride, List bufferBlendOverrides, CustomUniforms customUniforms, Supplier flipState, float alphaTest, boolean containsTessellation, CallbackInfo ci) {
        if (!Config.isModEnabled)
            return;

        fadeInterface = new FadeShaderInterface(context);
    }

    @Override
    public void setFadeDatas(GlMutableBuffer buffer) {
        fadeInterface.setFadeDatas(buffer);
    }
}
