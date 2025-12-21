package com.koteinik.chunksfadein.compat.sodium;

import com.koteinik.chunksfadein.compat.sodium.ext.*;
import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.DataBuffer;

public class ChunkFadeInController {
	private final DataBuffer chunkFadeDatasBuffer = new DataBuffer(RenderRegionExt.REGION_SIZE, 4);
	private GlMutableBufferExt chunkGlFadeDataBuffer;
	private boolean dirty = true;

	public ChunkFadeInController(CommandListExt commandList) {
		chunkGlFadeDataBuffer = commandList.makeMutableBuffer();

		for (int i = 0; i < RenderRegionExt.REGION_SIZE; i++)
			completeChunkFade(i, true);
	}

	public float[] getChunkData(int chunkId) {
		float x = chunkFadeDatasBuffer.get(chunkId, 0);
		float y = chunkFadeDatasBuffer.get(chunkId, 1);
		float z = chunkFadeDatasBuffer.get(chunkId, 2);
		float w = chunkFadeDatasBuffer.get(chunkId, 3);

		return new float[] {
			x, y, z, w
		};
	}

	public void completeChunkFade(int chunkId, boolean completeFade) {
		chunkFadeDatasBuffer.put(chunkId, 0, 0f);
		chunkFadeDatasBuffer.put(chunkId, 1, 0f);
		chunkFadeDatasBuffer.put(chunkId, 2, 0f);
		if (completeFade)
			chunkFadeDatasBuffer.put(chunkId, 3, 1f);
	}

	public void resetFadeForChunk(int chunkId) {
		chunkFadeDatasBuffer.put(chunkId, 0, 0f);
		chunkFadeDatasBuffer.put(chunkId, 1, Config.isAnimationEnabled ? Config.animationOffset : 0f);
		chunkFadeDatasBuffer.put(chunkId, 2, 0f);
		chunkFadeDatasBuffer.put(chunkId, 3, Config.isFadeEnabled ? 0f : 1f);
	}

	public void uploadToBuffer(ChunkShaderInterfaceExt shader, CommandListExt commandList) {
		if (dirty) {
			chunkFadeDatasBuffer.uploadData(commandList, chunkGlFadeDataBuffer);
			dirty = false;
		}

		shader.bindUniforms(chunkGlFadeDataBuffer);
	}

	public void processChunk(RenderSectionExt section, int sectionIndex) {
		long delta = section.calculateAndGetDelta();

		section.dhMarkRendered();

		if (Config.isFadeEnabled)
			dirty |= section.incrementFadeCoeff(delta, sectionIndex, chunkFadeDatasBuffer);
		else if (chunkFadeDatasBuffer.get(sectionIndex, 3) != 1f)
			chunkFadeDatasBuffer.put(sectionIndex, 3, 1f);

		if (Config.isAnimationEnabled)
			dirty |= section.incrementAnimationOffset(delta, sectionIndex, chunkFadeDatasBuffer);
		else
			for (int i = 0; i < 3; i++)
				if (chunkFadeDatasBuffer.get(sectionIndex, i) != 0f)
					chunkFadeDatasBuffer.put(sectionIndex, i, 0f);

		section.setRenderedBefore();
	}

	public void delete(CommandListExt commandList) {
		chunkFadeDatasBuffer.delete();

		if (chunkGlFadeDataBuffer != null)
			commandList.deleteBuffer(chunkGlFadeDataBuffer);
	}
}
