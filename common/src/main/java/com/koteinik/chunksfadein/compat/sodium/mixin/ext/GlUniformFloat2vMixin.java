package com.koteinik.chunksfadein.compat.sodium.mixin.ext;

import com.koteinik.chunksfadein.compat.sodium.ext.GlUniformFloat2vExt;
import net.caffeinemc.mods.sodium.client.gl.shader.uniform.GlUniformFloat2v;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = GlUniformFloat2v.class, remap = false)
public abstract class GlUniformFloat2vMixin implements GlUniformFloat2vExt {
	@Shadow
	public abstract void set(float x, float y);

	@Override
	public void set(int x, int y) {
		set((float) x, (float) y);
	}
}
