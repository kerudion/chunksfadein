package com.koteinik.chunksfadein.mixin.iris;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.koteinik.chunksfadein.Logger;
import com.koteinik.chunksfadein.config.Config;
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
    private static boolean isOldIris;

    static {
        try {
            isOldIris = FabricLoader.getInstance().getModContainer("iris").get()
                    .getMetadata()
                    .getVersion().compareTo(Version.parse("1.4")) < 0;
        } catch (VersionParsingException e) {
            isOldIris = false;
        }
    }

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
        if (isOldIris)
            fragmentInjector.appendToFunction("void main()",
                    "iris_FragData[0] = mix(iris_FragData[0], iris_FogColor, 1.0 - fadeCoeff);");
        else
            fragmentInjector.appendToFunction("void main()",
                    "if(fadeCoeff != 0.0) ${uniform_0} = ${mix_uniform_0_and_fog};");
    }

    @ModifyVariable(method = "createVertexShader", at = @At(value = "STORE", ordinal = 0), remap = false)
    private String modifyCreateVertexShader(String irisVertexShader) {
        if (irisVertexShader == null)
            return null;

        if (!Config.isModEnabled)
            return irisVertexShader;

        String code = vertexInjector.get(irisVertexShader);
        return code;
    }

    @ModifyVariable(method = "createFragmentShader", at = @At(value = "STORE", ordinal = 0), remap = false)
    private String modifyCreateFragmentShader(String irisFragmentShader) {
        if (irisFragmentShader == null)
            return null;

        if (!Config.isModEnabled)
            return irisFragmentShader;

        String code = fragmentInjector.get(irisFragmentShader);
        return code;
    }
}
