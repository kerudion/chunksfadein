package com.koteinik.chunksfadein.core;

import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.extensions.ChunkShaderInterfaceExt;
import com.koteinik.chunksfadein.extensions.RenderSectionExt;

import me.jellysquid.mods.sodium.client.gl.buffer.GlMutableBuffer;
import me.jellysquid.mods.sodium.client.gl.device.CommandList;
import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegion;

public class ChunkFadeInController {
    private final DataBuffer chunkFadeDatasBuffer = new DataBuffer(RenderRegion.REGION_SIZE, 4);
    private GlMutableBuffer chunkGlFadeDataBuffer;

    public ChunkFadeInController() {
        for (int i = 0; i < RenderRegion.REGION_SIZE; i++)
            completeChunkFade(i, true);
    }

    public void createBuffer(CommandList list) {
        chunkGlFadeDataBuffer = list.createMutableBuffer();
    }

    public float[] getChunkData(int chunkId) {
        float x = chunkFadeDatasBuffer.get(chunkId, 0);
        float y = chunkFadeDatasBuffer.get(chunkId, 1);
        float z = chunkFadeDatasBuffer.get(chunkId, 2);
        float w = chunkFadeDatasBuffer.get(chunkId, 3);

        return new float[] { x, y, z, w };
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
        chunkFadeDatasBuffer.put(chunkId, 1, Config.isAnimationEnabled ? -Config.animationInitialOffset : 0f);
        chunkFadeDatasBuffer.put(chunkId, 2, 0f);
        chunkFadeDatasBuffer.put(chunkId, 3, Config.isFadeEnabled ? 0f : 1f);
    }

    public void uploadToBuffer(ChunkShaderInterfaceExt shader, CommandList commandList) {
        chunkFadeDatasBuffer.uploadData(commandList, chunkGlFadeDataBuffer);
        shader.setFadeDatas(chunkGlFadeDataBuffer);
    }

    public void processChunk(RenderSectionExt chunk, int chunkId, int x, int y, int z) {
        long delta = chunk.calculateAndGetDelta();

        if (Config.isFadeEnabled)
            chunkFadeDatasBuffer.put(chunkId, 3, chunk.incrementFadeCoeff(delta));
        else if (chunkFadeDatasBuffer.get(chunkId, 3) != 1f)
            chunkFadeDatasBuffer.put(chunkId, 3, 1f);

        if (Config.isAnimationEnabled) {
            float[] animationProgress = chunk.incrementAnimationOffset(delta);

            chunkFadeDatasBuffer.put(chunkId, 0, animationProgress[0]);
            chunkFadeDatasBuffer.put(chunkId, 1, animationProgress[1]);
            chunkFadeDatasBuffer.put(chunkId, 2, animationProgress[2]);
        } else if (chunkFadeDatasBuffer.get(chunkId, 1) != 0f)
            chunkFadeDatasBuffer.put(chunkId, 1, 0f);

        chunk.setRenderedBefore();
    }

    public void delete(CommandList commandList) {
        chunkFadeDatasBuffer.delete();

        if (chunkGlFadeDataBuffer != null)
            commandList.deleteBuffer(chunkGlFadeDataBuffer);
    }
}
