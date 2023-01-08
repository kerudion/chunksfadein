package com.koteinik.chunksfadein.mixin.iris;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.koteinik.chunksfadein.core.ShaderInjector;

import me.jellysquid.mods.sodium.client.gl.shader.GlShader;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.IrisChunkProgramOverrides;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.IrisTerrainPass;
import net.coderbot.iris.pipeline.SodiumTerrainPipeline;

@Pseudo
@Mixin(value = IrisChunkProgramOverrides.class)
public class IrisChunkProgramOverridesMixin {
    private static final ShaderInjector vertexInjector = new ShaderInjector();
    private static final ShaderInjector fragmentInjector = new ShaderInjector();
    private IrisTerrainPass fragmentPass;

    static {
        vertexInjector.addCode(1,
                "out float fadeCoeff;",
                "struct ChunkFadeData {",
                "    vec4 fadeData;",
                "};",
                "layout(std140) uniform ubo_ChunkFadeDatas {",
                "    ChunkFadeData Chunk_FadeDatas[256];",
                "};");
        vertexInjector.appendToFunction("void _vert_init()",
                "fadeCoeff = Chunk_FadeDatas[_draw_id].fadeData.w;",
                "_vert_position = _vert_position + Chunk_FadeDatas[_draw_id].fadeData.xyz;");

        fragmentInjector.addCode(1,
                "in float fadeCoeff;");
        fragmentInjector.appendToFunction("void main()",
                "${uniform_0} = ${mix_uniform_0_and_fog};");
    }

    @ModifyVariable(method = "createVertexShader", at = @At(value = "STORE", ordinal = 0), remap = false)
    private String modifyCreateVertexShader(String irisVertexShader) {
        if (irisVertexShader == null)
            return null;

        return vertexInjector.get(irisVertexShader);
    }

    @ModifyVariable(method = "createFragmentShader", at = @At(value = "STORE", ordinal = 0), remap = false)
    private String modifyCreateFragmentShader(String irisFragmentShader) {
        if (irisFragmentShader == null)
            return null;

        if (fragmentPass != IrisTerrainPass.GBUFFER_CUTOUT)
            return irisFragmentShader;

        return fragmentInjector.get(irisFragmentShader);
    }

    @Inject(method = "createFragmentShader", at = @At(value = "HEAD"), remap = false)
    private void modifyCreateFragmentShader(IrisTerrainPass pass, SodiumTerrainPipeline pipeline,
            CallbackInfoReturnable<GlShader> shader) {
        fragmentPass = pass;
    }
}
