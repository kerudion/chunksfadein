package com.koteinik.chunksfadein.mixin.iris;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.FadeShaderInterface;
import com.koteinik.chunksfadein.extensions.ChunkShaderInterfaceExt;

import me.jellysquid.mods.sodium.client.gl.buffer.GlMutableBuffer;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderOptions;
import net.irisshaders.iris.compat.sodium.impl.shader_overrides.IrisChunkShaderInterface;
import net.irisshaders.iris.compat.sodium.impl.shader_overrides.ShaderBindingContextExt;
import net.irisshaders.iris.gl.blending.BlendModeOverride;
import net.irisshaders.iris.gl.blending.BufferBlendOverride;
import net.irisshaders.iris.pipeline.SodiumTerrainPipeline;
import net.irisshaders.iris.uniforms.custom.CustomUniforms;

@Pseudo
@Mixin(value = IrisChunkShaderInterface.class, remap = false)
public class IrisChunkShaderInterfaceMixin implements ChunkShaderInterfaceExt {
    private FadeShaderInterface fadeInterface;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void modifyConstructor(int var1, ShaderBindingContextExt ext, SodiumTerrainPipeline var3,
        ChunkShaderOptions var4, boolean var5, boolean var6, BlendModeOverride var7, List<BufferBlendOverride> var8, float var9,
        CustomUniforms var10,
        CallbackInfo ci) {
        if (!Config.isModEnabled)
            return;

        fadeInterface = new FadeShaderInterface(ext);
    }

    @Override
    public void setFadeDatas(GlMutableBuffer buffer) {
        fadeInterface.setFadeDatas(buffer);
    }
}
