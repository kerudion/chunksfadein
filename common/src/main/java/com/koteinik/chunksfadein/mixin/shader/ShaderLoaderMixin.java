package com.koteinik.chunksfadein.mixin.shader;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.koteinik.chunksfadein.core.FadeShader;
import com.koteinik.chunksfadein.core.ShaderInjector;
import com.koteinik.chunksfadein.hooks.CompatibilityHook;

import net.caffeinemc.mods.sodium.client.gl.shader.ShaderLoader;
import net.minecraft.resources.ResourceLocation;

@Mixin(value = ShaderLoader.class, remap = false)
public abstract class ShaderLoaderMixin {
    @Inject(method = "getShaderSource", at = @At("RETURN"), cancellable = true)
    private static void modifyConstructor(ResourceLocation name, CallbackInfoReturnable<String> cir) {
        if (CompatibilityHook.isIrisShaderPackInUse())
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
        FadeShader shader = new FadeShader();

        injector.insertAfterInVars(shader.fragInVars().dumpMultiline());

        injector.appendToFunction("void main()",
            shader.fragColorMod("{frag_color}", "u_FogColor").dumpMultiline());

        return injector;
    }

    private static ShaderInjector prepareVertexInjector() {
        ShaderInjector injector = new ShaderInjector();
        FadeShader shader = new FadeShader();

        injector.insertAfterOutVars(shader.vertOutUniforms().dumpMultiline());

        injector.insertAfterVariable("vec3 position",
            shader.vertMod("_vert_position", "position", false, "{mesh_id}").dumpMultiline());

        return injector;
    }
}
