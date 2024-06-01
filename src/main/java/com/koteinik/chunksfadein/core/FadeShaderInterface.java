package com.koteinik.chunksfadein.core;

import me.jellysquid.mods.sodium.client.gl.buffer.GlMutableBuffer;
import me.jellysquid.mods.sodium.client.gl.shader.uniform.GlUniformBlock;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ShaderBindingContext;
import net.irisshaders.iris.compat.sodium.impl.shader_overrides.ShaderBindingContextExt;

public class FadeShaderInterface {
    private GlUniformBlock uniformFadeDatas;

    public FadeShaderInterface(Object context) {
        if (context instanceof ShaderBindingContext)
            this.uniformFadeDatas = ((ShaderBindingContext) context).bindUniformBlock("ubo_ChunkFadeDatas", 1);
        else if (context instanceof ShaderBindingContextExt)
            this.uniformFadeDatas = ((ShaderBindingContextExt) context).bindUniformBlockIfPresent("ubo_ChunkFadeDatas", 1);
    }

    public void setFadeDatas(GlMutableBuffer buffer) {
        uniformFadeDatas.bindBuffer(buffer);
    }
}
