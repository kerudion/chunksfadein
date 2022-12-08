package com.koteinik.chunksfadein.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.koteinik.chunksfadein.Config;
import com.koteinik.chunksfadein.extenstions.ChunkShaderInterfaceExt;

import me.jellysquid.mods.sodium.client.gl.buffer.GlMutableBuffer;
import me.jellysquid.mods.sodium.client.gl.shader.uniform.GlUniformBlock;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderInterface;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderOptions;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ShaderBindingContext;

@Mixin(value = ChunkShaderInterface.class, remap = false)
public abstract class ChunkShaderInterfaceMixin implements ChunkShaderInterfaceExt {
    private final boolean needToDisable = Config.needToTurnOff();

    private GlUniformBlock uniformFadeCoeffs;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void modifyShaderForFadeInEffect(ShaderBindingContext context, ChunkShaderOptions options,
            CallbackInfo ci) {
        if (needToDisable)
            return;

        uniformFadeCoeffs = context.bindUniformBlock("ubo_ChunkFadeDatas", 1);
    }

    @Override
    public void setFadeCoeffs(GlMutableBuffer buffer) {
        uniformFadeCoeffs.bindBuffer(buffer);
    }
}