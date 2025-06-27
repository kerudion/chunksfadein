package com.koteinik.chunksfadein.core;

import com.koteinik.chunksfadein.Logger;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

public class SkyFBO {
	private static SkyFBO instance = null;

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

	public static int getTextureId() {
		SkyFBO instance = getInstance();
		if (instance == null) return -1;

		return instance.textureId;
	}

	public static void active(int texture) {
		SkyFBO instance = getInstance();
		if (instance == null) return;

		instance.activeTexture(texture);
	}

	public static void bindAttachment(int attachment) {
		SkyFBO instance = getInstance();
		if (instance == null) return;

		instance.bindColorAttachment(attachment);
	}

	public final int id;
	public final int textureId;
	public final int width;
	public final int height;

	private int cachedReadId = -1;

	public SkyFBO(int width, int height) throws Exception {
		this.width = width;
		this.height = height;

		this.id = GL30.glGenFramebuffers();

		GlStateSaver.GlSavedState state = GlStateSaver.saveState();

		GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, id);

		this.textureId = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);

		GlStateManager._texImage2D(
			GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA,
			width, height,
			0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, null
		);

		GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

		GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

		GlStateManager._glFramebufferTexture2D(
			GL30.GL_FRAMEBUFFER,
			GL30.GL_COLOR_ATTACHMENT0,
			GL11.GL_TEXTURE_2D,
			textureId,
			0
		);

		checkStatus(GL30.GL_FRAMEBUFFER);

		state.restore();
	}

	public void blitFromTexture(int srcTex, int srcWidth, int srcHeight, boolean restoreState) {
		if (cachedReadId == -1) cachedReadId = GL30.glGenFramebuffers();

		int lastRead = restoreState ? GlStateManager.getFrameBuffer(GL30.GL_READ_FRAMEBUFFER) : 0;
		int lastDraw = restoreState ? GlStateManager.getFrameBuffer(GL30.GL_DRAW_FRAMEBUFFER) : 0;

		GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, cachedReadId);
		GlStateManager._glFramebufferTexture2D(
			GL30.GL_READ_FRAMEBUFFER,
			GL30.GL_COLOR_ATTACHMENT0,
			GL11.GL_TEXTURE_2D,
			srcTex,
			0
		);
		GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, id);

		GlStateManager._glBlitFrameBuffer(
			0, 0, srcWidth, srcHeight,
			0, 0, width, height,
			GL11.GL_COLOR_BUFFER_BIT,
			GL11.GL_NEAREST
		);

		GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, lastRead);
		GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, lastDraw);
	}

	public void blitFromFramebuffer(int buffer, int srcWidth, int srcHeight, boolean restoreState) {
		int lastRead = restoreState ? GlStateManager.getFrameBuffer(GL30.GL_READ_FRAMEBUFFER) : 0;
		int lastDraw = restoreState ? GlStateManager.getFrameBuffer(GL30.GL_DRAW_FRAMEBUFFER) : 0;

		GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, buffer);
		GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, id);

		GlStateManager._glBlitFrameBuffer(
			0, 0, srcWidth, srcHeight,
			0, 0, width, height,
			GL11.GL_COLOR_BUFFER_BIT,
			GL11.GL_NEAREST
		);

		GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, lastRead);
		GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, lastDraw);
	}

	public void bindColorAttachment(int attachment) {
		GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, attachment, GL11.GL_TEXTURE_2D, textureId, 0);

		try (MemoryStack stack = MemoryStack.stackPush()) {
			IntBuffer drawBuffers = stack.mallocInt(2);
			drawBuffers.put(GL30.GL_COLOR_ATTACHMENT0);
			drawBuffers.put(attachment);
			drawBuffers.flip();
			GL20.glDrawBuffers(drawBuffers);
		}
	}

	public void activeTexture(int slot) {
		GL13.glActiveTexture(GL13.GL_TEXTURE0 + slot);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
	}

	public void cleanup() {
		if (id != -1) GL30.glDeleteFramebuffers(id);
		if (textureId != -1) GL11.glDeleteTextures(textureId);
		if (cachedReadId != -1) GL30.glDeleteFramebuffers(cachedReadId);
	}

	private static void checkStatus(int target) throws Exception {
		int status = GL30.glCheckFramebufferStatus(target);
		if (status != GL30.GL_FRAMEBUFFER_COMPLETE) {
			GL30.glBindFramebuffer(target, 0);
			throw new Exception("Framebuffer incomplete: " + status);
		}
	}
}
