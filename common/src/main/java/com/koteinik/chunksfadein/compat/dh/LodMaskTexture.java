package com.koteinik.chunksfadein.compat.dh;

import com.koteinik.chunksfadein.Logger;
import com.koteinik.chunksfadein.core.GlStateSaver;
import com.koteinik.chunksfadein.core.Utils;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class LodMaskTexture {
	private static LodMaskTexture instance = null;
	private static int lastLevel = 0;

	public static synchronized void createAndUpdate() {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level == null) return;

		int minY = minecraft.level.getMinSection();
		int maxY = minecraft.level.getMaxSection();

		int renderDistance = Utils.chunkRenderDistance();
		int sizeX = renderDistance * 2 + 1;
		int sizeY = maxY - minY + 1;
		int sizeZ = renderDistance * 2 + 1;
		if (sizeX <= 0 || sizeY <= 0 || sizeZ <= 0) return;

		Vec3 cameraPos = Utils.cameraPosition();
		ChunkPos origin = new ChunkPos(
			(int) Math.floor(cameraPos.x / 16),
			(int) Math.floor(cameraPos.z / 16)
		);

		try {
			if (instance == null) {
				instance = new LodMaskTexture(sizeX, sizeY, sizeZ, minY, maxY, origin, null);
			} else if (instance.sizeX != sizeX || instance.sizeY != sizeY || instance.sizeZ != sizeZ
				|| instance.minY != minY || instance.maxY != maxY) {
				instance.cleanup();
				instance = new LodMaskTexture(sizeX, sizeY, sizeZ, minY, maxY, origin, instance.rendered);
			}
		} catch (Exception e) {
			Logger.error("Failed to create LodMaskTexture: ", e);
			instance = null;
		}

		int currentLevel = minecraft.level.hashCode();
		if (lastLevel != currentLevel)
			instance.rendered.clear();
		lastLevel = currentLevel;

		instance.origin = origin;
		instance.update();
	}

	public static LodMaskTexture getInstance() {
		return instance;
	}

	public static int getId() {
		LodMaskTexture instance = getInstance();

		if (instance == null) return -1;
		else return instance.id;
	}

	public static void markRendered(int chunkX, int chunkY, int chunkZ) {
		if (instance != null) instance.markRendered(SectionPos.of(chunkX, chunkY, chunkZ));
	}

	public static void bind(int slot) {
		if (instance != null) instance.bindTexture(slot);
	}

	public final int id;
	public final int sizeX;
	public final int sizeY;
	public final int sizeZ;
	public final int minY;
	public final int maxY;

	public ChunkPos origin;

	private final ByteBuffer textureDataBuffer;
	private final Set<SectionPos> rendered;

	private boolean needUpdate = false;

	private LodMaskTexture(int sizeX, int sizeY, int sizeZ, int minY, int maxY, ChunkPos origin, Set<SectionPos> rendered) {
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.sizeZ = sizeZ;
		this.minY = minY;
		this.maxY = maxY;
		this.origin = origin;
		this.rendered = rendered == null ? ConcurrentHashMap.newKeySet(sizeX * sizeY * sizeZ) : rendered;

		this.textureDataBuffer = MemoryUtil.memAlloc(sizeX * sizeY * sizeZ * 4);

		this.id = GL11.glGenTextures();

		GlStateSaver.withSavedState(() -> {
			GL13.glActiveTexture(GL13.GL_TEXTURE0);
			GL11.glBindTexture(GL12.GL_TEXTURE_3D, id);

			clearGlState();

			GL12.glTexImage3D(
				GL12.GL_TEXTURE_3D, 0, GL30.GL_RGBA8,
				sizeX, sizeY, sizeZ,
				0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null
			);

			GL11.glTexParameteri(GL12.GL_TEXTURE_3D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
			GL11.glTexParameteri(GL12.GL_TEXTURE_3D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
			GL11.glTexParameteri(GL12.GL_TEXTURE_3D, GL12.GL_TEXTURE_WRAP_S, GL14.GL_CLAMP_TO_BORDER);
			GL11.glTexParameteri(GL12.GL_TEXTURE_3D, GL12.GL_TEXTURE_WRAP_T, GL14.GL_CLAMP_TO_BORDER);
			GL11.glTexParameteri(GL12.GL_TEXTURE_3D, GL12.GL_TEXTURE_WRAP_R, GL14.GL_CLAMP_TO_BORDER);
			GL11.glTexParameterfv(GL12.GL_TEXTURE_3D, GL14.GL_TEXTURE_BORDER_COLOR, new float[] { 0f, 0f, 0f, 0f });
		});
	}

	public synchronized void update() {
		RenderSystem.assertOnRenderThread();

		int renderDistance = Utils.chunkRenderDistance();

		textureDataBuffer.clear();
		for (int z = 0; z < sizeZ; z++)
			for (int y = 0; y < sizeY; y++)
				for (int x = 0; x < sizeX; x++) {
					//					byte val = (byte) (((float) z / (float) (sizeZ - 1)) * 255.0f);
					//					byte val = (byte) (((float) x / (float) (sizeX - 1)) * 255.0f);
					//					byte val = x % 2 == z % 2 ? (byte) 255 : (byte) 0;

					int centerX = sizeX / 2;
					int centerZ = sizeZ / 2;
					boolean wasRendered = rendered.remove(SectionPos.of(
						origin.x - centerX + x,
						minY + y,
						origin.z - centerZ + z
					));
					// this gap is required so that there are no holes when chunks unload, I couldn't find a better way :(
					if (Math.floor(Math.sqrt(Math.pow(x - centerX, 2) + Math.pow(z - centerZ, 2))) >= renderDistance)
						wasRendered = false;

					byte val = (byte) (wasRendered ? 255 : 0);
					needUpdate |= textureDataBuffer.get(textureDataBuffer.position()) != val;

					textureDataBuffer.put(val);
					textureDataBuffer.put((byte) 0);
					textureDataBuffer.put((byte) 0);
					textureDataBuffer.put((byte) 0);
				}
		textureDataBuffer.flip();
		rendered.clear();

		if (!needUpdate) return;

		GlStateSaver.withSavedState(() -> {
			GL13.glActiveTexture(GL13.GL_TEXTURE0);
			GL11.glBindTexture(GL12.GL_TEXTURE_3D, id);

			clearGlState();

			GL12.glTexSubImage3D(
				GL12.GL_TEXTURE_3D, 0, 0, 0, 0,
				sizeX, sizeY, sizeZ,
				GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, textureDataBuffer
			);
		});

		needUpdate = false;
	}

	public synchronized void markRendered(SectionPos pos) {
		rendered.add(pos);
	}

	public void bindTexture(int slot) {
		GL13.glActiveTexture(GL13.GL_TEXTURE0 + slot);
		GL11.glBindTexture(GL12.GL_TEXTURE_3D, id);
	}

	public void cleanup() {
		if (id != -1)
			GL11.glDeleteTextures(id);
		if (textureDataBuffer != null)
			MemoryUtil.memFree(textureDataBuffer);
	}

	private void clearGlState() {
		GL21.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, 0);
		GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 4);
		GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH, 0);
		GL11.glPixelStorei(GL12.GL_UNPACK_SKIP_PIXELS, 0);
		GL11.glPixelStorei(GL12.GL_UNPACK_SKIP_ROWS, 0);
		GL11.glPixelStorei(GL12.GL_UNPACK_SKIP_IMAGES, 0);
		GL11.glPixelStorei(GL12.GL_UNPACK_IMAGE_HEIGHT, 0);
		GL11.glPixelStorei(GL11.GL_UNPACK_SWAP_BYTES, GL11.GL_FALSE);
		GL11.glPixelStorei(GL11.GL_UNPACK_LSB_FIRST, GL11.GL_FALSE);
	}
}
