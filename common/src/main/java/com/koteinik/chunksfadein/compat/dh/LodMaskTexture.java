package com.koteinik.chunksfadein.compat.dh;

import com.koteinik.chunksfadein.Logger;
import com.koteinik.chunksfadein.core.Utils;
import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.opengl.GlBuffer;
import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.systems.RenderSystem;
import net.caffeinemc.mods.sodium.client.gpu.device.backend.DrawBackend;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL33C;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static com.mojang.blaze3d.buffers.GpuBuffer.*;
import static com.mojang.blaze3d.systems.RenderSystem.getDevice;
import static org.lwjgl.system.MemoryUtil.memFree;

public class LodMaskTexture {
	private static LodMaskTexture instance = null;
	private static int lastLevel = 0;

	public static void prepare() {
		RenderSystem.assertOnRenderThread();

		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level == null) return;

		int minY = minecraft.level.getMinSectionY();
		int maxY = minecraft.level.getMaxSectionY();

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
				instance = new LodMaskTexture(sizeX, sizeY, sizeZ, minY, maxY, origin);
			} else if (instance.sizeX != sizeX || instance.sizeY != sizeY || instance.sizeZ != sizeZ
				|| instance.minY != minY || instance.maxY != maxY) {
				instance.cleanup();
				instance = new LodMaskTexture(sizeX, sizeY, sizeZ, minY, maxY, origin);
			}
		} catch (Exception e) {
			Logger.error("Failed to create LodMaskTexture: ", e);
			instance = null;
		}

		int currentLevel = minecraft.level.hashCode();
		if (lastLevel != currentLevel)
			Arrays.fill(instance.rendered, false);
		lastLevel = currentLevel;

		instance.origin = origin;
	}

	public static void upload() {
		if (instance != null)
			instance.update();
	}

	public static Gl getGlInstance() {
		return instance == null ? null : instance.gl;
	}

	public static LodMaskTexture getInstance() {
		return instance;
	}

	public static void markRendered(int chunkX, int chunkY, int chunkZ) {
		if (instance != null) instance.markChunk(chunkX, chunkY, chunkZ);
	}

	public final int sizeX;
	public final int sizeY;
	public final int sizeZ;
	public final int minY;
	public final int maxY;

	public ChunkPos origin;

	public final Gl gl;

	private final GpuBuffer textureBuffer;
	private final ByteBuffer textureDataBuffer;
	private final boolean[] rendered;

	private boolean needUpdate = false;

	private LodMaskTexture(int sizeX, int sizeY, int sizeZ, int minY, int maxY, ChunkPos origin) {
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.sizeZ = sizeZ;
		this.minY = minY;
		this.maxY = maxY;
		this.origin = origin;
		this.rendered = new boolean[sizeX * sizeY * sizeZ];

		this.textureDataBuffer = MemoryUtil.memAlloc(sizeX * sizeY * sizeZ);

		this.textureBuffer = getDevice().createBuffer(
			() -> "Chunks Fade In DH lod mask buffer",
			USAGE_UNIFORM_TEXEL_BUFFER | USAGE_COPY_DST | USAGE_MAP_WRITE,
			textureDataBuffer.capacity()
		);

		if (DrawBackend.BACKEND == DrawBackend.OPENGL)
			this.gl = new Gl();
		else
			this.gl = null;
	}

	public GpuBuffer getBuffer() {
		return textureBuffer;
	}

	public void update() {
		RenderSystem.assertOnRenderThread();

		int renderDistance = Utils.chunkRenderDistance();

		textureDataBuffer.clear();
		int i = 0;
		for (int z = 0; z < sizeZ; z++)
			for (int y = 0; y < sizeY; y++)
				for (int x = 0; x < sizeX; x++) {
					//					byte val = (byte) (((float) z / (float) (sizeZ - 1)) * 255.0f);
					//					byte val = (byte) (((float) x / (float) (sizeX - 1)) * 255.0f);
					//					byte val = x % 2 == z % 2 ? (byte) 255 : (byte) 0;

					int centerX = sizeX / 2;
					int centerZ = sizeZ / 2;
					boolean wasRendered = rendered[i];
					// this gap is required so that there are no holes when chunks unload, I couldn't find a better way :(
					if (Math.floor(Math.sqrt(Math.pow(x - centerX, 2) + Math.pow(z - centerZ, 2))) >= renderDistance)
						wasRendered = false;

					byte val = (byte) (wasRendered ? 255 : 0);
					needUpdate |= textureDataBuffer.get(textureDataBuffer.position()) != val;

					textureDataBuffer.put(val);

					rendered[i] = false;

					i++;
				}
		textureDataBuffer.flip();

		if (!needUpdate) return;

		getDevice().createCommandEncoder().writeToBuffer(
			textureBuffer.slice(),
			textureDataBuffer
		);

		needUpdate = false;
	}

	public void markChunk(int chunkX, int chunkY, int chunkZ) {
		RenderSystem.assertOnRenderThread();

		int x = chunkX - (origin.x() - sizeX / 2);
		int y = chunkY - minY;
		int z = chunkZ - (origin.z() - sizeZ / 2);

		if (x >= 0 && x < sizeX &&
			y >= 0 && y < sizeY &&
			z >= 0 && z < sizeZ) {
			int i = (z * sizeY * sizeX) + (y * sizeX) + x;

			rendered[i] = true;
		}
	}

	public void cleanup() {
		textureBuffer.close();
		memFree(textureDataBuffer);

		if (gl != null)
			gl.cleanup();
	}

	public class Gl {
		private int texture = -1;

		public void bindTexture(int slot) {
			if (!(textureBuffer instanceof GlBuffer buffer))
				return;

			if (texture == -1) texture = GL13.glGenTextures();

			int prevActive = GL13.glGetInteger(GL13.GL_ACTIVE_TEXTURE);

			GL13.glActiveTexture(GL13.GL_TEXTURE0 + slot);
			GL11.glBindTexture(GL31.GL_TEXTURE_BUFFER, texture);
			GL33C.glTexBuffer(
				GL31.GL_TEXTURE_BUFFER,
				GlConst.toGlInternalId(GpuFormat.R8_SINT),
				buffer.handle()
			);

			GL13.glActiveTexture(prevActive);
		}

		public void cleanup() {
			if (texture != -1)
				GL11.glDeleteTextures(texture);

			texture = -1;
		}
	}
}
