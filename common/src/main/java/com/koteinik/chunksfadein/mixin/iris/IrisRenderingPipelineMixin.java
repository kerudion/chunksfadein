package com.koteinik.chunksfadein.mixin.iris;

import com.koteinik.chunksfadein.Logger;
import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.IrisPatcher;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.programs.ShaderKey;
import net.irisshaders.iris.pipeline.programs.ShaderSupplier;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import net.irisshaders.iris.shaderpack.programs.ProgramSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;
import java.util.Optional;

@Mixin(value = IrisRenderingPipeline.class, remap = false)
public class IrisRenderingPipelineMixin {
	private static Field VERTEX_FIELD = null;
	private static Field FRAGMENT_FIELD = null;

	static {
		try {
			VERTEX_FIELD = ProgramSource.class.getDeclaredField("vertexSource");
			VERTEX_FIELD.setAccessible(true);

			FRAGMENT_FIELD = ProgramSource.class.getDeclaredField("fragmentSource");
			FRAGMENT_FIELD.setAccessible(true);
		} catch (Exception e) {
			Logger.error("Failed to get Iris fields:", e);
		}
	}

	@Inject(
		method = "createShader(Ljava/lang/String;Ljava/util/Optional;Lnet/irisshaders/iris/pipeline/programs/ShaderKey;)Lnet/irisshaders/iris/pipeline/programs/ShaderSupplier;",
		at = @At("HEAD"))
	private void modifyCreateShaderSource(String name, Optional<ProgramSource> sourceOptional, ShaderKey key, CallbackInfoReturnable<ShaderSupplier> cir) {
		if (!Config.isModEnabled)
			return;

		if (sourceOptional.isEmpty())
			return;

		ProgramSource source = sourceOptional.get();

		try {
			Optional<String> vert = source.getVertexSource();
			if (vert.isPresent())
				VERTEX_FIELD.set(source, IrisPatcher.injectVarsAndDummyAPI(PatchShaderType.VERTEX, vert.get()));

			Optional<String> frag = source.getFragmentSource();
			if (frag.isPresent())
				FRAGMENT_FIELD.set(source, IrisPatcher.injectVarsAndDummyAPI(PatchShaderType.FRAGMENT, frag.get()));
		} catch (Exception e) {
			Logger.error("Failed to set Iris fields:", e);
		}
	}
}
