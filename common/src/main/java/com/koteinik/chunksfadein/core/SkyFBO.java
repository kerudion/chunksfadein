package com.koteinik.chunksfadein.core;

import com.koteinik.chunksfadein.Logger;
import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.opengl.FrameBufferAttachment;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import net.caffeinemc.mods.sodium.client.gpu.device.backend.DrawBackend;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

public class SkyFBO {
	private static SkyFBO instance = null;

	public static Gl getGlInstance() {
		return getInstance().gl;
	}

	public static synchronized SkyFBO getInstance() {
		Window window = Minecraft.getInstance().getWindow();
		int windowWidth = window.getWidth();
		int windowHeight = window.getHeight();

		try {
			if (instance == null) {
				instance = new SkyFBO(windowWidth, windowHeight);
			} else if (instance.width != windowWidth || instance.height != windowHeight) {
				instance.cleanup();
				instance = new SkyFBO(windowWidth, windowHeight);
			}
		} catch (Exception e) {
			Logger.error("Failed to create SkyFBO: ", e);
			instance = null;
		}

		return instance;
	}

	public static int getWidth() {
		SkyFBO instance = getInstance();
		if (instance == null) return -1;

		return instance.width;
	}

	public static int getHeight() {
		SkyFBO instance = getInstance();
		if (instance == null) return -1;

		return instance.height;
	}

	public final int width;
	public final int height;

	public TextureTarget texture;
	public final Gl gl;

	public SkyFBO(int width, int height) {
		this.width = width;
		this.height = height;

		this.texture = new TextureTarget(
			"Chunks Fade In sky + DH texture",
			width,
			height,
			false,
			GpuFormat.RGBA8_UNORM
		);

		if (DrawBackend.BACKEND == DrawBackend.OPENGL)
			this.gl = new Gl();
		else
			this.gl = null;
	}

	public void blitFromTexture(GpuTexture from) {
		if (texture.getColorTexture() == null) return;

		RenderSystem.getDevice().createCommandEncoder().copyTextureToTexture(
			from,
			texture.getColorTexture(), 0,
			0, 0,
			0, 0,
			Math.min(width, from.getWidth(0)), Math.min(height, from.getHeight(0))
		);
	}

	public void cleanup() {
		texture.destroyBuffers();
		if (gl != null) gl.cleanup();
	}

	public class Gl {
		private int drawFbo = -1;
		private int readFbo = -1;

		public void blitFromTexture(int srcTex, int srcWidth, int srcHeight) {
			if (!(texture.getColorTexture() instanceof FrameBufferAttachment dst))
				return;

			if (drawFbo == -1) drawFbo = GL30.glGenFramebuffers();
			if (readFbo == -1) readFbo = GL30.glGenFramebuffers();

			int lastRead = GlStateManager.getFrameBuffer(GL30.GL_READ_FRAMEBUFFER);
			int lastReadTexId = GL30.glGetFramebufferAttachmentParameteri(
				GL30.GL_READ_FRAMEBUFFER,
				GL30.GL_COLOR_ATTACHMENT0,
				GL30.GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME
			);
			int lastReadMipLevel = GL30.glGetFramebufferAttachmentParameteri(
				GL30.GL_READ_FRAMEBUFFER,
				GL30.GL_COLOR_ATTACHMENT0,
				GL30.GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_LEVEL
			);
			int lastDraw = GlStateManager.getFrameBuffer(GL30.GL_DRAW_FRAMEBUFFER);
			int lastDrawTexId = GL30.glGetFramebufferAttachmentParameteri(
				GL30.GL_DRAW_FRAMEBUFFER,
				GL30.GL_COLOR_ATTACHMENT0,
				GL30.GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME
			);
			int lastDrawMipLevel = GL30.glGetFramebufferAttachmentParameteri(
				GL30.GL_DRAW_FRAMEBUFFER,
				GL30.GL_COLOR_ATTACHMENT0,
				GL30.GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_LEVEL
			);

			GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, readFbo);
			GlStateManager._glFramebufferTexture2D(
				GL30.GL_READ_FRAMEBUFFER,
				GL30.GL_COLOR_ATTACHMENT0,
				GL11.GL_TEXTURE_2D,
				srcTex, 0
			);

			GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, drawFbo);
			GlStateManager._glFramebufferTexture2D(
				GL30.GL_DRAW_FRAMEBUFFER,
				GL30.GL_COLOR_ATTACHMENT0,
				GL11.GL_TEXTURE_2D,
				dst.glId(), dst.fboMipLevel()
			);

			GlStateManager._glBlitFrameBuffer(
				0, 0, srcWidth, srcHeight,
				0, 0, width, height,
				GL11.GL_COLOR_BUFFER_BIT,
				GL11.GL_NEAREST
			);

			GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, lastRead);
			GlStateManager._glFramebufferTexture2D(
				GL30.GL_READ_FRAMEBUFFER,
				GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D,
				lastReadTexId, lastReadMipLevel
			);
			GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, lastDraw);
			GlStateManager._glFramebufferTexture2D(
				GL30.GL_DRAW_FRAMEBUFFER,
				GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D,
				lastDrawTexId, lastDrawMipLevel
			);
		}

		public void blitFromFramebuffer(int buffer, int srcWidth, int srcHeight) {
			if (!(texture.getColorTexture() instanceof FrameBufferAttachment dst))
				return;

			if (drawFbo == -1) drawFbo = GL30.glGenFramebuffers();

			int lastRead = GlStateManager.getFrameBuffer(GL30.GL_READ_FRAMEBUFFER);
			int lastDraw = GlStateManager.getFrameBuffer(GL30.GL_DRAW_FRAMEBUFFER);
			int lastDrawTexId = GL30.glGetFramebufferAttachmentParameteri(
				GL30.GL_DRAW_FRAMEBUFFER,
				GL30.GL_COLOR_ATTACHMENT0,
				GL30.GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME
			);
			int lastDrawMipLevel = GL30.glGetFramebufferAttachmentParameteri(
				GL30.GL_DRAW_FRAMEBUFFER,
				GL30.GL_COLOR_ATTACHMENT0,
				GL30.GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_LEVEL
			);

			GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, buffer);

			GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, drawFbo);
			GlStateManager._glFramebufferTexture2D(
				GL30.GL_DRAW_FRAMEBUFFER,
				GL30.GL_COLOR_ATTACHMENT0,
				GL11.GL_TEXTURE_2D,
				dst.glId(), dst.fboMipLevel()
			);

			GlStateManager._glBlitFrameBuffer(
				0, 0, srcWidth, srcHeight,
				0, 0, width, height,
				GL11.GL_COLOR_BUFFER_BIT,
				GL11.GL_NEAREST
			);

			GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, lastRead);
			GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, lastDraw);
			GlStateManager._glFramebufferTexture2D(
				GL30.GL_DRAW_FRAMEBUFFER,
				GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D,
				lastDrawTexId, lastDrawMipLevel
			);
		}

		public void bindColorAttachment(int attachment) {
			GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, attachment, GL11.GL_TEXTURE_2D, textureId(), 0);

			try (MemoryStack stack = MemoryStack.stackPush()) {
				IntBuffer drawBuffers = stack.mallocInt(2);
				drawBuffers.put(GL30.GL_COLOR_ATTACHMENT0);
				drawBuffers.put(attachment);
				drawBuffers.flip();
				GL20.glDrawBuffers(drawBuffers);
			}
		}

		public void bindTexture(int slot) {
			int prevActive = GL13.glGetInteger(GL13.GL_ACTIVE_TEXTURE);

			GL13.glActiveTexture(GL13.GL_TEXTURE0 + slot);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId());

			GL13.glActiveTexture(prevActive);
		}

		public int textureId() {
			if (texture.getColorTexture() instanceof GlTexture glTexture)
				return glTexture.glId();
			else
				return -1;
		}

		private void cleanup() {
			if (readFbo != -1) GlStateManager._glDeleteFramebuffers(readFbo);
			if (drawFbo != -1) GlStateManager._glDeleteFramebuffers(drawFbo);
			readFbo = -1;
			drawFbo = -1;
		}
	}
}
