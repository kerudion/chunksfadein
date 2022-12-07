package com.koteinik.chunksfadein.mixin;

import me.jellysquid.mods.sodium.client.gl.shader.ShaderLoader;
import net.minecraft.util.Identifier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.koteinik.chunksfadein.Config;

@Mixin(value = ShaderLoader.class, remap = false)
public abstract class ShaderLoaderMixin {
    @Inject(method = "getShaderSource", at = @At("RETURN"), cancellable = true)
    private static void modifyShaderForFadeInEffect(Identifier name, CallbackInfoReturnable<String> cir) {
        if (Config.needToTurnOff())
            return;

        String path = name.getPath();

        String[] splittedPath = path.split("/");
        String shaderFileName = splittedPath[splittedPath.length - 1];

        String source = cir.getReturnValue();

        switch (shaderFileName) {
            case "fog.glsl":
                source = source
                        .replaceFirst("float fogEnd\\)", "float fogEnd, float fadeCoeff)")
                        .replaceFirst("float fadeCoeff\\) \\{",
                                "float fadeCoeff) {\n    fragColor = mix(fragColor, fogColor, 1.0 - fadeCoeff);\n");
                break;

            case "block_layer_opaque.fsh":
                source = source
                        .replaceFirst("in vec4", "in float v_fadeCoeff;\nin vec4")
                        .replaceFirst("u_FogEnd\\);", "u_FogEnd, v_fadeCoeff);");
                break;

            case "block_layer_opaque.vsh":
                source = source
                        .replaceFirst("out", "out float v_fadeCoeff;\nout")
                        .replaceFirst("_vert_tex_diffuse_coord;",
                                "_vert_tex_diffuse_coord;\n    v_fadeCoeff = _fade_coeff;");
                break;

            case "chunk_vertex.glsl":
                source += "\n#define _fade_coeff Chunk_FadeCoeffs[_draw_id].fadeCoeff.x";
                break;

            case "chunk_parameters.glsl":
                source += "\n\nstruct ChunkFadeCoeff {\n    vec4 fadeCoeff;\n};";
                source += "\n\nlayout(std140) uniform ubo_ChunkFadeCoeffs {\n    ChunkFadeCoeff Chunk_FadeCoeffs[256];\n};";
                break;

            default:
                break;
        }

        cir.setReturnValue(source);
    }
}
