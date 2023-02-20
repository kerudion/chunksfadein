package com.koteinik.chunksfadein.mixin.iris;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.koteinik.chunksfadein.Logger;
import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.FadeTypes;
import com.koteinik.chunksfadein.core.ShaderInjector;

import me.jellysquid.mods.sodium.client.gl.shader.GlShader;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.IrisChunkProgramOverrides;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.IrisTerrainPass;
import net.coderbot.iris.pipeline.SodiumTerrainPipeline;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;

@Pseudo
@Mixin(value = IrisChunkProgramOverrides.class)
public class IrisChunkProgramOverridesMixin {
    private static final ShaderInjector vertexInjectorFull = new ShaderInjector();
    private static final ShaderInjector fragmentInjectorFull = new ShaderInjector();
    
    private static final ShaderInjector vertexInjectorLined = new ShaderInjector();
    private static final ShaderInjector fragmentInjectorLined = new ShaderInjector();
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
        vertexInjectorFull.insertAfterDefines(
                "out float fadeCoeff;",
                "struct ChunkFadeData {",
                "    vec4 fadeData;",
                "};",
                "layout(std140) uniform ubo_ChunkFadeDatas {",
                "    ChunkFadeData Chunk_FadeDatas[256];",
                "};");
        vertexInjectorFull.appendToFunction("void _vert_init()",
                "fadeCoeff = Chunk_FadeDatas[_draw_id].fadeData.w;",
                "_vert_position = _vert_position + Chunk_FadeDatas[_draw_id].fadeData.xyz;");
        vertexInjectorLined.copyFrom(vertexInjectorFull);

        vertexInjectorLined.insertAfterDefines("out float localHeight;");
        vertexInjectorLined.appendToFunction("void _vert_init()",
                "localHeight = _vert_position.y;");

        fragmentInjectorFull.insertAfterDefines("in float fadeCoeff;");

        fragmentInjectorLined.copyFrom(fragmentInjectorFull);
        fragmentInjectorLined.insertAfterDefines("in float localHeight;");

        fragmentInjectorLined.appendToFunction("void main()",
                "float fadeLineY = fadeCoeff * 16.0;");
        if (isOldIris) {
            fragmentInjectorFull.appendToFunction("void main()",
                    "if(fadeCoeff != 0.0) iris_FragData[0] = mix(iris_FragData[0], iris_FogColor, 1.0 - fadeCoeff);");

            fragmentInjectorLined.appendToFunction("void main()",
                    "if(fadeCoeff != 0.0) iris_FragData[0] = mix(iris_FragData[0], iris_FogColor, localHeight <= fadeLineY ? 0.0 : 1.0);");
        } else {
            fragmentInjectorFull.appendToFunction("void main()",
                    "if(fadeCoeff != 0.0) ${uniform_0} = ${uniform_0_prefix}mix(${uniform_0}, iris_FogColor, 1.0 - fadeCoeff)${uniform_0_postfix};");

            fragmentInjectorLined.appendToFunction("void main()",
                    "if(fadeCoeff != 0.0) ${uniform_0} = ${uniform_0_prefix}mix(${uniform_0}, iris_FogColor, localHeight <= fadeLineY ? 0.0 : 1.0)${uniform_0_postfix};");
        }
    }

    @ModifyVariable(method = "createVertexShader", at = @At(value = "STORE", ordinal = 0), remap = false)
    private String modifyCreateVertexShader(String irisVertexShader) {
        if (irisVertexShader == null)
            return null;

        if (!Config.isModEnabled)
            return irisVertexShader;

        String code = (Config.fadeType == FadeTypes.FULL ? vertexInjectorFull : vertexInjectorLined)
                .get(irisVertexShader);
        return code;
    }

    @ModifyVariable(method = "createFragmentShader", at = @At(value = "STORE", ordinal = 0), remap = false)
    private String modifyCreateFragmentShader(String irisFragmentShader) {
        if (irisFragmentShader == null)
            return null;

        if (!Config.isModEnabled)
            return irisFragmentShader;

        String code = (Config.fadeType == FadeTypes.FULL ? fragmentInjectorFull : fragmentInjectorLined)
                .get(irisFragmentShader);
        return code;
    }
}
