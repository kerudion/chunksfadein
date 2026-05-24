package com.koteinik.chunksfadein.compat.sodium;

import com.koteinik.chunksfadein.compat.sodium.ext.GlUniformMatrix3fExt;
import me.jellysquid.mods.sodium.client.gl.shader.uniform.GlUniform;
import org.joml.Matrix3f;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

public class GlUniformMatrix3f extends GlUniform<Matrix3f> implements GlUniformMatrix3fExt {
	public GlUniformMatrix3f(int index) {
		super(index);
	}

	@Override
	public void set(Matrix3f mat) {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			FloatBuffer buf = stack.mallocFloat(9);
			mat.get(buf);
			GL20.glUniformMatrix3fv(this.index, false, buf);
		}
	}
}
