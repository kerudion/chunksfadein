package com.koteinik.chunksfadein.compat.sodium.mixin.ext;

import com.koteinik.chunksfadein.compat.sodium.ext.GlUniformBlockExt;
import com.koteinik.chunksfadein.compat.sodium.ext.GlUniformFloat2vExt;
import com.koteinik.chunksfadein.compat.sodium.ext.ShaderBindingContextExt;
import net.caffeinemc.mods.sodium.client.gl.shader.GlProgram;
import net.caffeinemc.mods.sodium.client.gl.shader.uniform.GlUniform;
import net.caffeinemc.mods.sodium.client.gl.shader.uniform.GlUniformBlock;
import net.caffeinemc.mods.sodium.client.gl.shader.uniform.GlUniformFloat2v;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.IntFunction;

@Mixin(value = GlProgram.class, remap = false)
public abstract class ShaderBindingContextMixin implements ShaderBindingContextExt {
	@Shadow
	public abstract @Nullable GlUniformBlock bindUniformBlockOptional(String s, int i);

	@Shadow
	public abstract <U extends GlUniform<?>> @Nullable U bindUniformOptional(String var1, IntFunction<U> var2);

	@Override
	public GlUniformBlockExt bindUniformBlock(String name) {
		return (GlUniformBlockExt) bindUniformBlockOptional(name, 1);
	}

	@Override
	public GlUniformFloat2vExt bindUniformFloat2v(String name) {
		return (GlUniformFloat2vExt) bindUniformOptional(name, GlUniformFloat2v::new);
	}
}
