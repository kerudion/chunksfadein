package com.koteinik.chunksfadein.compat.dh.mixin.blaze;

import com.koteinik.chunksfadein.compat.dh.ext.GlDeviceExt;
import com.mojang.blaze3d.opengl.GlProgram;
import com.mojang.blaze3d.opengl.GlRenderPipeline;
import com.mojang.blaze3d.opengl.GlShaderModule;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.lang.reflect.Method;
import java.util.Map;

@Mixin(targets = "com.mojang.blaze3d.opengl.GlDevice")
public abstract class GlDeviceMixin implements GlDeviceExt {
	@Unique
	private static Method cfi_idMethod;

	static {
		try {
			Class<?> clazz = Class.forName("com.mojang.blaze3d.opengl.GlDevice$ShaderCompilationKey");

			cfi_idMethod = clazz.getMethod("id");
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	@Shadow
	@Final
	private Map<RenderPipeline, GlRenderPipeline> pipelineCache;

	@Shadow
	@Final
	private Map<Object, GlShaderModule> shaderCache;

	@Override
	public void cfi_evictPipeline(RenderPipeline pipeline) {
		if (pipeline == null) return;

		GlRenderPipeline cached = pipelineCache.remove(pipeline);
		if (cached != null && cached.program() != GlProgram.INVALID_PROGRAM)
			cached.program().close();

		Identifier vert = pipeline.getVertexShader();
		Identifier frag = pipeline.getFragmentShader();
		shaderCache.entrySet().removeIf(e -> {
			try {
				Identifier id = (Identifier) cfi_idMethod.invoke(e.getKey());
				if (id.equals(vert) || id.equals(frag)) {
					GlShaderModule module = e.getValue();
					if (module != GlShaderModule.INVALID_SHADER)
						module.close();

					return true;
				}
			} catch (Throwable t) {
				t.printStackTrace();
			}
			return false;
		});
	}
}
