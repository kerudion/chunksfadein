package com.koteinik.chunksfadein.mixin.shader;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.FadeTypes;
import com.koteinik.chunksfadein.hooks.IrisApiHook;

import me.jellysquid.mods.sodium.client.gl.shader.ShaderLoader;
import net.minecraft.util.Identifier;

@Mixin(value = ShaderLoader.class, remap = false)
public abstract class ShaderLoaderMixin {
    @Inject(method = "getShaderSource", at = @At("RETURN"), cancellable = true)
    private static void modifyConstructor(Identifier name, CallbackInfoReturnable<String> cir) {
        if (!Config.isModEnabled || IrisApiHook.isShaderPackInUse())
            return;

        String path = name.getPath();

        String[] splittedPath = path.split("/");
        String shaderFileName = splittedPath[splittedPath.length - 1];

        String source = cir.getReturnValue();

        switch (shaderFileName) {
            case "fog.glsl":
                if (Config.fadeType == FadeTypes.FULL)
                    source = source
                            .replaceFirst("float fogEnd\\)", "float fogEnd, float fadeCoeff)")
                            .replaceFirst("float fadeCoeff\\) \\{",
                                    "float fadeCoeff) {\n    fragColor = mix(fragColor, fogColor, 1.0 - fadeCoeff);\n");
                else
                    source = source
                            .replaceFirst("float fogEnd\\)", "float fogEnd, float fadeCoeff, float localHeight)")
                            .replaceFirst("float localHeight\\) \\{",
                                    "float localHeight) {\n    float fadeLineY = fadeCoeff * 16.0;\n    fragColor = mix(fragColor, fogColor, localHeight <= fadeLineY ? 0.0 : 1.0);\n");
                break;

            case "block_layer_opaque.fsh":
                if (Config.fadeType == FadeTypes.FULL)
                    source = source
                            .replaceFirst("in vec4", "in float v_fadeCoeff;\nin vec4")
                            .replaceFirst("u_FogEnd\\);", "u_FogEnd, v_fadeCoeff);");
                else
                    source = source
                            .replaceFirst("in vec4", "in float v_fadeCoeff;\nin float v_localHeight;\nin vec4")
                            .replaceFirst("u_FogEnd\\);", "u_FogEnd, v_fadeCoeff, v_localHeight);");
                break;

            case "block_layer_opaque.vsh":
                if (Config.fadeType == FadeTypes.FULL)
                    source = source
                            .replaceFirst("out", "out float v_fadeCoeff;\nout")
                            .replaceFirst("_vert_tex_diffuse_coord;",
                                    "_vert_tex_diffuse_coord;\n    v_fadeCoeff = _fade_coeff;")
                            .replaceFirst("\\+ _vert_position;", "+ _vert_position + _fade_offset;");
                else
                    source = source
                            .replaceFirst("out", "out float v_fadeCoeff;\nout float v_localHeight;\nout")
                            .replaceFirst("_vert_tex_diffuse_coord;",
                                    "_vert_tex_diffuse_coord;\n    v_fadeCoeff = _fade_coeff;\n    v_localHeight = _vert_position.y;")
                            .replaceFirst("\\+ _vert_position;", "+ _vert_position + _fade_offset;");
                break;

            case "chunk_vertex.glsl":
                source += "\n#define _fade_offset Chunk_FadeDatas[_draw_id].fadeData.xyz";
                source += "\n#define _fade_coeff Chunk_FadeDatas[_draw_id].fadeData.w";
                break;

            case "chunk_parameters.glsl":
                source += "\n\nstruct ChunkFadeData {\n    vec4 fadeData;\n};";
                source += "\n\nlayout(std140) uniform ubo_ChunkFadeDatas {\n    ChunkFadeData Chunk_FadeDatas[256];\n};";
                break;

            default:
                break;
        }

        cir.setReturnValue(source);
    }
}
