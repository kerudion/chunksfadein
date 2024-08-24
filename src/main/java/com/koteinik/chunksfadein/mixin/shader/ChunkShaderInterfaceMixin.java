package com.koteinik.chunksfadein.mixin.shader;

import net.caffeinemc.mods.sodium.client.render.chunk.shader.DefaultShaderInterface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.koteinik.chunksfadein.Logger;
import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.FadeShaderInterface;
import com.koteinik.chunksfadein.extensions.ChunkShaderInterfaceExt;
import com.koteinik.chunksfadein.hooks.CompatibilityHook;

import net.caffeinemc.mods.sodium.client.gl.buffer.GlMutableBuffer;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ChunkShaderInterface;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ChunkShaderOptions;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ShaderBindingContext;

@Mixin(value = DefaultShaderInterface.class, remap = false)
public abstract class ChunkShaderInterfaceMixin implements ChunkShaderInterfaceExt {
    private FadeShaderInterface fadeInterface;
    private static boolean warned = false;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void modifyConstructor(ShaderBindingContext context, ChunkShaderOptions options,
            CallbackInfo ci) {
        if (!Config.isModEnabled || CompatibilityHook.isIrisShaderPackInUse())
            return;

        fadeInterface = new FadeShaderInterface(context);
    }

    @Override
    public void setFadeDatas(GlMutableBuffer buffer) {
        if (fadeInterface == null) {
            if (CompatibilityHook.isIrisShaderPackInUse() && !warned) {
                Logger.warn("Shader pack is in use, but Sodium's shader interface is used. Something went really wrong!");
                warned = true;
            }
            return;
        }

        fadeInterface.setFadeDatas(buffer);
    }
}