package com.koteinik.chunksfadein.mixin.shader;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.FadeShaderInterface;
import com.koteinik.chunksfadein.extenstions.ChunkShaderInterfaceExt;
import com.koteinik.chunksfadein.hooks.IrisApiHook;

import me.jellysquid.mods.sodium.client.gl.buffer.GlMutableBuffer;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderInterface;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderOptions;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ShaderBindingContext;

@Mixin(value = ChunkShaderInterface.class, remap = false)
public abstract class ChunkShaderInterfaceMixin implements ChunkShaderInterfaceExt {
    private FadeShaderInterface fadeInterface;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void modifyConstructor(ShaderBindingContext context, ChunkShaderOptions options,
            CallbackInfo ci) {
        if (!Config.isModEnabled || IrisApiHook.isShaderPackInUse())
            return;

        fadeInterface = new FadeShaderInterface(context);
    }

    @Override
    public void setFadeDatas(GlMutableBuffer buffer) {
        fadeInterface.setFadeDatas(buffer);
    }
}