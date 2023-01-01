package com.koteinik.chunksfadein.mixin.iris;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.koteinik.chunksfadein.Logger;
import com.koteinik.chunksfadein.core.ShaderInjector;

import net.coderbot.iris.compat.sodium.impl.shader_overrides.IrisChunkProgramOverrides;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;

@Pseudo
@Mixin(value = IrisChunkProgramOverrides.class)
public class IrisChunkProgramOverridesMixin {
    private static final ShaderInjector vertexInjector = new ShaderInjector();
    private static final ShaderInjector fragmentInjector = new ShaderInjector();

    static {
        boolean isIrisV12 = false;
        try {
            isIrisV12 = FabricLoader.getInstance().getModContainer("iris").get().getMetadata()
                    .getVersion().compareTo(Version.parse("1.4")) < 0;
        } catch (VersionParsingException e) {
        }

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
        if (isIrisV12)
            fragmentInjector.appendToFunction("void main()",
                    "iris_FragData[0] = mix(iris_FragData[0], iris_FogColor, 1.0 - fadeCoeff);");
        else
            fragmentInjector.appendToFunction("void main()",
                    "iris_FragData0 = mix(iris_FragData0, iris_FogColor, 1.0 - fadeCoeff);");
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
