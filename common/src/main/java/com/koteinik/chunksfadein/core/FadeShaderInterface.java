package com.koteinik.chunksfadein.core;

import net.caffeinemc.mods.sodium.client.gl.buffer.GlMutableBuffer;
import net.caffeinemc.mods.sodium.client.gl.shader.uniform.GlUniformBlock;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ShaderBindingContext;

public class FadeShaderInterface {
    private GlUniformBlock uniformFadeDatas;

    public FadeShaderInterface(ShaderBindingContext context) {
        this.uniformFadeDatas = context.bindUniformBlock("ubo_ChunkFadeDatas", 1);
    }

    public void setFadeDatas(GlMutableBuffer buffer) {
        uniformFadeDatas.bindBuffer(buffer);
    }
}
