package com.koteinik.chunksfadein.compat.monocle.mixin;

import com.koteinik.chunksfadein.compat.monocle.MonoclePatcher;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.irisshaders.iris.gl.blending.AlphaTest;
import net.irisshaders.iris.gl.texture.TextureType;
import net.irisshaders.iris.helpers.Tri;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import net.irisshaders.iris.shaderpack.texture.TextureStage;
import org.embeddedt.embeddium.impl.render.chunk.vertex.format.ChunkVertexType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(targets = "dev.ferriarnus.monocle.ShaderTransformer", remap = false)
public class ShaderTransformerMixin {
	@Inject(method = "transform", at = @At("RETURN"))
	private static void modifyTransform(
		String name,
		String vertex,
		String geometry,
		String tessControl,
		String tessEval,
		String fragment,
		AlphaTest alpha,
		ChunkVertexType vertexType,
		Object2ObjectMap<Tri<String, TextureType, TextureStage>, String> textureMap,
		CallbackInfoReturnable<Map<PatchShaderType, String>> cir
	) {
		Map<PatchShaderType, String> map = cir.getReturnValue();
		if (map == null) return;

		String frag = map.get(PatchShaderType.FRAGMENT);
		if (frag != null && !frag.contains("cfi_sky"))
			map.put(PatchShaderType.FRAGMENT, MonoclePatcher.patch(PatchShaderType.FRAGMENT, frag));

		String vert = map.get(PatchShaderType.VERTEX);
		if (vert != null && !vert.contains("cfi_ChunkFadeData"))
			map.put(PatchShaderType.VERTEX, MonoclePatcher.patch(PatchShaderType.VERTEX, vert));
	}
}
