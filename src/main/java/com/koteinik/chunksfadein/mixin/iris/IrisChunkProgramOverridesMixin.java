package com.koteinik.chunksfadein.mixin.iris;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.koteinik.chunksfadein.Logger;
import com.koteinik.chunksfadein.core.ShaderInjector;

import io.github.douira.glsl_transformer.ast.transform.ASTInjectionPoint;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.IrisChunkProgramOverrides;

@Pseudo
@Mixin(value = IrisChunkProgramOverrides.class)
public class IrisChunkProgramOverridesMixin {
    private static final ShaderInjector vertexInjector = new ShaderInjector();
    private static final ShaderInjector fragmentInjector = new ShaderInjector();

    static {
        vertexInjector.injectTo(ASTInjectionPoint.BEFORE_DECLARATIONS,
                "out float fadeCoeff;");
        vertexInjector.injectTo(ASTInjectionPoint.BEFORE_DECLARATIONS,
                "layout(std140) uniform ubo_ChunkFadeDatas { ChunkFadeData Chunk_FadeDatas[256]; };");
        vertexInjector.injectTo(ASTInjectionPoint.BEFORE_DECLARATIONS,
                "struct ChunkFadeData { vec4 fadeData; };");
        vertexInjector.appendToFunction("_vert_init",
                "fadeCoeff = Chunk_FadeDatas[_draw_id].fadeData.w;");
        vertexInjector.appendToFunction("_vert_init",
                "_vert_position = _vert_position + Chunk_FadeDatas[_draw_id].fadeData.xyz;");
        vertexInjector.commit();

        fragmentInjector.injectTo(ASTInjectionPoint.BEFORE_DECLARATIONS,
                "in float fadeCoeff;");
        fragmentInjector.appendToFunction("main",
                "iris_FragData0 = mix(iris_FragData0, iris_FogColor, 1.0 - fadeCoeff);");
        fragmentInjector.commit();
    }

    @ModifyVariable(method = "createVertexShader", at = @At(value = "STORE", ordinal = 0), remap = false)
    private String modifyCreateVertexShader(String irisVertexShader) {
        if (irisVertexShader == null)
            return null;

        Logger.info("----- VERTEX -----");
        Logger.info(irisVertexShader);
        String newCode = vertexInjector.get(irisVertexShader);
        Logger.info("----- VERTEX NEW -----");
        Logger.info(newCode);
        return newCode;
    }

    @ModifyVariable(method = "createFragmentShader", at = @At(value = "STORE", ordinal = 0), remap = false)
    private String modifyCreateFragmentShader(String irisFragmentShader) {
        if (irisFragmentShader == null)
            return null;

        Logger.info("----- FRAGMENT -----");
        Logger.info(irisFragmentShader);
        String newCode = fragmentInjector.get(irisFragmentShader);
        Logger.info("----- FRAGMENT NEW -----");
        Logger.info(newCode);
        return newCode;
    }
}
