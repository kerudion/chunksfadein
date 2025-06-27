package com.koteinik.chunksfadein.core;

import com.mojang.blaze3d.opengl.GlTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class Utils {
	public static int mainTargetWidth() {
		return Minecraft.getInstance().getMainRenderTarget().width;
	}

	public static int mainTargetHeight() {
		return Minecraft.getInstance().getMainRenderTarget().height;
	}

	public static int mainColorTexture() {
		if (Minecraft.getInstance().getMainRenderTarget().getColorTexture() instanceof GlTexture texture)
			return texture.glId();
		else
			return -1;
	}

	public static Vec3 cameraPosition() {
		return Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
	}

	public static int chunkRenderDistance() {
		return (int) Math.floor(Minecraft.getInstance().gameRenderer.getRenderDistance() / 16);
	}

	public static void debugWriteTexture(int texture, int width, int height, File file) {
		if (file.exists()) return;
		file.getParentFile().mkdirs();

		ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
		GL12.glGetTexImage(GL12.GL_TEXTURE_2D, 0, GL12.GL_RGBA, GL12.GL_UNSIGNED_BYTE, buffer);

		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int i = (x + y * width) * 4;

				int r = buffer.get(i) & 0xFF;
				int g = buffer.get(i + 1) & 0xFF;
				int b = buffer.get(i + 2) & 0xFF;
				int a = buffer.get(i + 3) & 0xFF;

				int argb = (a << 24) | (r << 16) | (g << 8) | b;

				image.setRGB(x, height - 1 - y, argb);
			}
		}

		try {
			ImageIO.write(image, "png", file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
