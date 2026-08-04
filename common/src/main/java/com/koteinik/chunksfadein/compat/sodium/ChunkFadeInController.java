package com.koteinik.chunksfadein.compat.sodium;

import com.koteinik.chunksfadein.compat.sodium.ext.RenderSectionExt;
import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.Utils;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.CommandEncoder;
import net.minecraft.client.multiplayer.ClientLevel;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static com.mojang.blaze3d.buffers.GpuBuffer.*;
import static com.mojang.blaze3d.systems.RenderSystem.getDevice;
import static net.caffeinemc.mods.sodium.client.render.chunk.region.RenderRegion.REGION_HEIGHT;
import static net.caffeinemc.mods.sodium.client.render.chunk.region.RenderRegion.REGION_WIDTH;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memFree;

public class ChunkFadeInController {
	// Buffer logic based on Sodium's UniformBufferManager
	private final GpuBuffer uniformBuffer;
	private final GpuBuffer fadeBuffer;

	private final ByteBuffer fadeCpuBuffer;
	private final boolean[] regionsDirty;

	public ChunkFadeInController(ClientLevel level, int renderDistance) {
		uniformBuffer = getDevice().createBuffer(
			() -> "Chunks Fade In uniform buffer",
			USAGE_UNIFORM | USAGE_COPY_DST | USAGE_MAP_WRITE,
			16
		);

		int renderDistDim = (2 * renderDistance) + 1;
		int totalVertDist = level.getMaxSectionY() - level.getMinSectionY() + 1;

		int regionsX = (renderDistDim + (2 * REGION_WIDTH) - 2) / REGION_WIDTH;
		int regionsY = (totalVertDist + (2 * REGION_HEIGHT) - 2) / REGION_HEIGHT;

		int maxRegions = regionsX * regionsY * regionsX * 2;

		fadeCpuBuffer = memAlloc(maxRegions * 256 * 4 * 4);
		regionsDirty = new boolean[maxRegions];

		fadeBuffer = getDevice().createBuffer(
			() -> "Chunks Fade In fade buffer",
			USAGE_UNIFORM_TEXEL_BUFFER | USAGE_COPY_DST | USAGE_MAP_WRITE,
			maxRegions * 256L * 4L * 4L
		);

		for (int i = 3; i < (fadeBuffer.size() / 4); i += 4)
			writeRaw(i * 4, 1f);

		Arrays.fill(regionsDirty, true);
	}

	public GpuBuffer getFadeBuffer() {
		return fadeBuffer;
	}

	public GpuBuffer getUniformBuffer() {
		return uniformBuffer;
	}

	public void updateUniforms() {
		try (MemoryStack stack = stackPush()) {
			ByteBuffer data = stack.malloc(8);
			data.putFloat(Utils.mainTargetWidth());
			data.putFloat(Utils.mainTargetHeight());
			data.flip();

			getDevice().createCommandEncoder()
				.writeToBuffer(uniformBuffer.slice(0, 8), data);
		}
	}

	public void processChunk(RenderSectionExt section, int regionIndex, int sectionIndex) {
		long delta = section.calculateAndGetDelta();

		section.dhMarkRendered();

		if (Config.isFadeEnabled)
			section.incrementFadeCoeff(delta, regionIndex, sectionIndex, this);
		else
			write(regionIndex, sectionIndex, 3, 1f);

		if (Config.isAnimationEnabled)
			section.incrementAnimationOffset(delta, regionIndex, sectionIndex, this);
		else
			write(regionIndex, sectionIndex, 0f, 0f, 0f);

		section.setRenderedBefore();
	}

	public void cleanRegion(int regionIndex) {
		for (int i = 0; i < 256; i++)
			write(regionIndex, i, 0f, 0f, 0f, 1f);
	}

	public void uploadToBuffer() {
		CommandEncoder encoder = getDevice().createCommandEncoder();

		for (int region = 0; region < regionsDirty.length; region++) {
			if (!regionsDirty[region]) continue;

			int offset = region * 256 * 16;
			encoder.writeToBuffer(
				fadeBuffer.slice(offset, 256 * 16),
				fadeCpuBuffer.slice(offset, 256 * 16)
			);

			regionsDirty[region] = false;
		}
	}

	public void write(int regionIndex, int sectionIndex, float x, float y, float z, float w) {
		if (writeRaw(((regionIndex * 256) + sectionIndex) * 4 * 4, x, y, z, w))
			regionsDirty[regionIndex] = true;
	}

	public void write(int regionIndex, int sectionIndex, float x, float y, float z) {
		if (writeRaw(((regionIndex * 256) + sectionIndex) * 4 * 4, x, y, z))
			regionsDirty[regionIndex] = true;
	}

	public void write(int regionIndex, int sectionIndex, int idx, float value) {
		if (writeRaw((((regionIndex * 256) + sectionIndex) * 4 + idx) * 4, value))
			regionsDirty[regionIndex] = true;
	}

	public boolean writeRaw(int idx, float x, float y, float z, float w) {
		boolean changed = false;
		changed |= writeRaw(idx, x);
		changed |= writeRaw(idx + 4, y);
		changed |= writeRaw(idx + 8, z);
		changed |= writeRaw(idx + 12, w);

		return changed;
	}

	public boolean writeRaw(int idx, float x, float y, float z) {
		boolean changed = false;
		changed |= writeRaw(idx, x);
		changed |= writeRaw(idx + 4, y);
		changed |= writeRaw(idx + 8, z);

		return changed;
	}

	public boolean writeRaw(int idx, float value) {
		if (idx + 4 > fadeCpuBuffer.capacity())
			throw new IllegalStateException("Chunk fade buffer index out of bounds: " + idx);

		if (fadeCpuBuffer.getFloat(idx) == value)
			return false;

		fadeCpuBuffer.putFloat(idx, value);
		return true;
	}

	public void delete() {
		uniformBuffer.close();
		fadeBuffer.close();
		memFree(fadeCpuBuffer);
	}
}
