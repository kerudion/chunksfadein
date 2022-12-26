package com.koteinik.chunksfadein.mixin.iris;

import java.util.List;

import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.FadeShaderInterface;
import com.koteinik.chunksfadein.extenstions.ChunkShaderInterfaceExt;
import com.koteinik.chunksfadein.hooks.IrisApiHook;

import me.jellysquid.mods.sodium.client.gl.buffer.GlMutableBuffer;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.IrisChunkShaderInterface;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.ShaderBindingContextExt;
import net.coderbot.iris.gl.blending.BlendModeOverride;
import net.coderbot.iris.gl.blending.BufferBlendOverride;
import net.coderbot.iris.pipeline.SodiumTerrainPipeline;

@Mixin(value = IrisChunkShaderInterface.class, remap = false)
public class IrisChunkShaderInterfaceMixin implements ChunkShaderInterfaceExt {
    private FadeShaderInterface fadeInterface;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void modifyConstructor(int var1, ShaderBindingContextExt ext, SodiumTerrainPipeline var3,
            boolean var4, BlendModeOverride var5, List<BufferBlendOverride> var6, float var7,
            CallbackInfo ci) {
        if (!Config.isModEnabled)
            return;

        fadeInterface = new FadeShaderInterface(ext);
    }

    @Inject(method = "setModelViewMatrix", at = @At("HEAD"))
    private void modifySetModelViewMatrix(Matrix4f var1, CallbackInfo ci) {
        IrisApiHook.irisExt = this;
    }

    @Override
    public void setFadeDatas(GlMutableBuffer buffer) {
        fadeInterface.setFadeDatas(buffer);
    }
}