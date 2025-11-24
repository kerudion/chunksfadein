package com.koteinik.chunksfadein.compat.embeddium;

import com.koteinik.chunksfadein.compat.sodium.ext.GlUniformFloat2vExt;
import org.embeddedt.embeddium.impl.gl.shader.uniform.GlUniform;
import org.lwjgl.opengl.GL30C;

public class GlUniformFloat2v extends GlUniform<float[]> implements GlUniformFloat2vExt {
	public GlUniformFloat2v(int index) {
		super(index);
	}

	public void set(float[] value) {
		if (value.length != 2) {
			throw new IllegalArgumentException("value.length != 2");
		} else {
			GL30C.glUniform2fv(this.index, value);
		}
	}

	public void set(float x, float y) {
		GL30C.glUniform2f(this.index, x, y);
	}

	@Override
	public void set(int x, int y) {
		set((float) x, (float) y);
	}
}
