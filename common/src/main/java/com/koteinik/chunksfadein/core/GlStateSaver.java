package com.koteinik.chunksfadein.core;

import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.*;

public class GlStateSaver {
	public static void withSavedState(Runnable action) {
		GlSavedState state = saveState();
		try {
			action.run();
		} finally {
			state.restore();
		}
	}

	public static GlSavedState saveState() {
		RenderSystem.assertOnRenderThread();

		return new GlSavedState(
			// Pixel store
			GL11.glGetInteger(GL11.GL_UNPACK_ALIGNMENT),
			GL11.glGetInteger(GL12.GL_UNPACK_SKIP_PIXELS),
			GL11.glGetInteger(GL11.GL_UNPACK_ROW_LENGTH),
			GL11.glGetInteger(GL12.GL_UNPACK_IMAGE_HEIGHT),
			GL11.glGetInteger(GL12.GL_UNPACK_SKIP_ROWS),
			GL11.glGetInteger(GL12.GL_UNPACK_SKIP_IMAGES),
			GL11.glGetBoolean(GL11.GL_UNPACK_SWAP_BYTES),
			GL11.glGetBoolean(GL11.GL_UNPACK_LSB_FIRST),

			// Binding
			GL11.glGetInteger(GL21.GL_PIXEL_UNPACK_BUFFER_BINDING),
			GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D),
			GL11.glGetInteger(GL12.GL_TEXTURE_BINDING_3D),
			GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING),
			GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING),
			GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE),

			// Depth
			GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK),
			GL11.glIsEnabled(GL11.GL_DEPTH_TEST),
			GL11.glGetInteger(GL11.GL_DEPTH_FUNC),

			// Blend
			GL11.glIsEnabled(GL11.GL_BLEND),
			GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB),
			GL11.glGetInteger(GL14.GL_BLEND_DST_RGB),
			GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA),
			GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA),
			GL11.glGetInteger(GL20.GL_BLEND_EQUATION_RGB),
			GL11.glGetInteger(GL20.GL_BLEND_EQUATION_ALPHA)
		);
	}

	public record GlSavedState(
		// Pixel store
		int unpackAlignment, int unpackSkipPixels, int unpackRowLength,
		int unpackImageHeight, int unpackSkipRows, int unpackSkipImages,
		boolean unpackSwapBytes, boolean unpackLsbFirst,

		// Binding
		int pixelUnpackBufferBinding, int textureBinding2D,
		int textureBinding3D,
		int readFramebufferBinding, int drawFramebufferBinding,
		int activeTextureUnit,

		// Depth
		boolean depthMask,
		boolean depthTestEnabled,
		int depthFunc,

		// Blend
		boolean blendEnabled,
		int blendSrcRgb,
		int blendDstRgb,
		int blendSrcAlpha,
		int blendDstAlpha,
		int blendEquationRgb,
		int blendEquationAlpha
	) {
		public void restore() {
			RenderSystem.assertOnRenderThread();

			// Pixel store
			GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, unpackAlignment);
			GL11.glPixelStorei(GL12.GL_UNPACK_SKIP_PIXELS, unpackSkipPixels);
			GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH, unpackRowLength);
			GL11.glPixelStorei(GL12.GL_UNPACK_IMAGE_HEIGHT, unpackImageHeight);
			GL11.glPixelStorei(GL12.GL_UNPACK_SKIP_ROWS, unpackSkipRows);
			GL11.glPixelStorei(GL12.GL_UNPACK_SKIP_IMAGES, unpackSkipImages);
			GL11.glPixelStorei(GL11.GL_UNPACK_SWAP_BYTES, unpackSwapBytes ? GL11.GL_TRUE : GL11.GL_FALSE);
			GL11.glPixelStorei(GL11.GL_UNPACK_LSB_FIRST, unpackLsbFirst ? GL11.GL_TRUE : GL11.GL_FALSE);

			// Binding
			GL21.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, pixelUnpackBufferBinding);

			GL13.glActiveTexture(activeTextureUnit);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureBinding2D);
			GL11.glBindTexture(GL12.GL_TEXTURE_3D, textureBinding3D);

			GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, readFramebufferBinding);
			GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, drawFramebufferBinding);

			// Depth
			GL11.glDepthMask(depthMask);
			if (depthTestEnabled) {
				GL11.glEnable(GL11.GL_DEPTH_TEST);
			} else {
				GL11.glDisable(GL11.GL_DEPTH_TEST);
			}
			GL11.glDepthFunc(depthFunc);

			// Blend
			if (blendEnabled) {
				GL11.glEnable(GL11.GL_BLEND);
			} else {
				GL11.glDisable(GL11.GL_BLEND);
			}
			GL20.glBlendEquationSeparate(blendEquationRgb, blendEquationAlpha);
			GL14.glBlendFuncSeparate(blendSrcRgb, blendDstRgb, blendSrcAlpha, blendDstAlpha);
		}
	}
}
