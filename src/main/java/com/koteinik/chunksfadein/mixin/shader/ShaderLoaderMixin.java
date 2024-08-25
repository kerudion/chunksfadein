package com.koteinik.chunksfadein.mixin.shader;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.FadeTypes;
import com.koteinik.chunksfadein.core.ShaderInjector;
import com.koteinik.chunksfadein.hooks.CompatibilityHook;

import net.caffeinemc.mods.sodium.client.gl.shader.ShaderLoader;
import net.minecraft.util.Identifier;

@Mixin(value = ShaderLoader.class, remap = false)
public abstract class ShaderLoaderMixin {
    @Inject(method = "getShaderSource", at = @At("RETURN"), cancellable = true)
    private static void modifyConstructor(Identifier name, CallbackInfoReturnable<String> cir) {
        if (!Config.isModEnabled || CompatibilityHook.isIrisShaderPackInUse())
            return;

        String path = name.getPath();

        String[] splittedPath = path.split("/");
        String shaderFileName = splittedPath[splittedPath.length - 1];

        String source = cir.getReturnValue();

        switch (shaderFileName) {
            case "block_layer_opaque.fsh":
                source = prepareFragmentInjector().get(source);
                break;

            case "block_layer_opaque.vsh":
                source = prepareVertexInjector().get(source);
                break;

            default:
                break;
        }

        cir.setReturnValue(source);
    }

    private static ShaderInjector prepareFragmentInjector() {
        ShaderInjector injector = new ShaderInjector();

        injector.insertAfterInVars("in float v_FadeCoeff;");

        if (Config.fadeType == FadeTypes.FULL) {
            injector.appendToFunction("void main()",
                "if(v_FadeCoeff >= 0.0 && v_FadeCoeff < 1.0) {frag_color} = mix({frag_color}, u_FogColor, 1.0 - v_FadeCoeff);");
        } else {
            injector.insertAfterInVars("in float v_LocalHeight;");
            injector.appendToFunction("void main()",
                "if(v_FadeCoeff >= 0.0 && v_FadeCoeff < 1.0) { float fadeLineY = v_FadeCoeff * 16.0; {frag_color} = mix({frag_color}, u_FogColor, v_LocalHeight <= fadeLineY ? 0.0 : 1.0); }");
        }

        return injector;
    }

    private static ShaderInjector prepareVertexInjector() {
        ShaderInjector injector = new ShaderInjector();

        injector.insertAfterOutVars(true, "out float v_FadeCoeff;");
        injector.insertAfterUniforms(
            "struct ChunkFadeData {",
            "    vec4 fadeData;",
            "};",
            "layout(std140) uniform ubo_ChunkFadeDatas {",
            "    ChunkFadeData Chunk_FadeDatas[256];",
            "};");

        if (Config.isCurvatureEnabled)
            injector.insertAfterVariable("vec3 position",
                "position.y -= dot(position, position) / %s;".formatted(Config.worldCurvature));

        injector.insertAfterVariable("vec3 position",
            "vec4 chunkFadeData = Chunk_FadeDatas[{mesh_id}].fadeData;",
            "position.y += chunkFadeData.y;");

        injector.appendToFunction("void main()",
            "v_FadeCoeff = chunkFadeData.w;");

        if (Config.fadeType == FadeTypes.LINED) {
            injector.insertAfterOutVars("out float v_LocalHeight;");
            injector.appendToFunction("void main()",
                "v_LocalHeight = _vert_position.y;");
        }

        return injector;
    }
}
