package com.koteinik.chunksfadein.mixin.shader;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.FadeTypes;
import com.koteinik.chunksfadein.core.ShaderInjector;
import com.koteinik.chunksfadein.hooks.CompatibilityHook;

import me.jellysquid.mods.sodium.client.gl.shader.ShaderLoader;
import net.minecraft.util.Identifier;

@Mixin(value = ShaderLoader.class, remap = false)
public abstract class ShaderLoaderMixin {
    private static final ShaderInjector fragmentInjectorFull = new ShaderInjector();
    private static final ShaderInjector fragmentInjectorLined = new ShaderInjector();

    private static final ShaderInjector vertexInjectorFull = new ShaderInjector();
    private static final ShaderInjector vertexInjectorLined = new ShaderInjector();

    static {
        fragmentInjectorFull.insertAfterInVars("in float v_FadeCoeff;");

        fragmentInjectorLined.copyFrom(fragmentInjectorFull);

        fragmentInjectorFull.appendToFunction("void main()",
                "if(v_FadeCoeff >= 0.0 && v_FadeCoeff < 1.0) fragColor = mix(fragColor, u_FogColor, 1.0 - v_FadeCoeff);");

        fragmentInjectorLined.insertAfterInVars("in float v_LocalHeight;");
        fragmentInjectorLined.appendToFunction("void main()",
                "if(v_FadeCoeff >= 0.0 && v_FadeCoeff < 1.0) { float fadeLineY = v_FadeCoeff * 16.0; fragColor = mix(fragColor, u_FogColor, v_LocalHeight <= fadeLineY ? 0.0 : 1.0); }");

        vertexInjectorFull.insertAfterOutVars("out float v_FadeCoeff;");
        vertexInjectorFull.insertAfterUniforms(
                "struct ChunkFadeData {",
                "    vec4 fadeData;",
                "};",
                "layout(std140) uniform ubo_ChunkFadeDatas {",
                "    ChunkFadeData Chunk_FadeDatas[256];",
                "};");
        vertexInjectorFull.insertAfterVariable("vec3 position",
                "vec4 fadeData = Chunk_FadeDatas[_draw_id].fadeData;",
                "position.y = position.y + fadeData.y;");
        vertexInjectorFull.appendToFunction("void main()",
                "v_FadeCoeff = fadeData.w;");

        vertexInjectorLined.copyFrom(vertexInjectorFull);
        vertexInjectorLined.insertAfterOutVars("out float v_LocalHeight;");
        vertexInjectorLined.appendToFunction("void main()",
                "v_LocalHeight = _vert_position.y;");
    }

    @Inject(method = "getShaderSource", at = @At("RETURN"), cancellable = true)
    private static void modifyConstructor(Identifier name, CallbackInfoReturnable<String> cir) {
        if (!Config.isModEnabled || CompatibilityHook.isIrisShaderPackInUse())
            return;

        String path = name.getPath();

        String[] splittedPath = path.split("/");
        String shaderFileName = splittedPath[splittedPath.length - 1];

        String source = cir.getReturnValue();
        boolean isFull = Config.fadeType == FadeTypes.FULL;

        switch (shaderFileName) {
            case "block_layer_opaque.fsh":
                source = (isFull ? fragmentInjectorFull : fragmentInjectorLined).get(source);
                break;

            case "block_layer_opaque.vsh":
                source = (isFull ? vertexInjectorFull : vertexInjectorLined).get(source);
                break;

            default:
                break;
        }

        cir.setReturnValue(source);
    }
}
