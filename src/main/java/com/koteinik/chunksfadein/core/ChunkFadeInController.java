package com.koteinik.chunksfadein.core;

import java.time.ZonedDateTime;
import java.util.List;

import com.koteinik.chunksfadein.Config;
import com.koteinik.chunksfadein.extenstions.ChunkShaderInterfaceExt;

import me.jellysquid.mods.sodium.client.gl.buffer.GlMutableBuffer;
import me.jellysquid.mods.sodium.client.gl.device.CommandList;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegion;

public class ChunkFadeInController {
    private final DataBuffer chunkFadeCoeffsBuffer = new DataBuffer(RenderRegion.REGION_SIZE, 4);
    private GlMutableBuffer chunkGlFadeCoeffBuffer;

    private long lastFrameTime = 0L;

    public void resetFadeCoeffForChunk(RenderSection chunk) {
        chunkFadeCoeffsBuffer.put(chunk.getChunkId(), 3, 0f);
    }

    public void updateChunksFade(List<RenderSection> chunks, ChunkShaderInterfaceExt shader, CommandList commandList) {
        checkMutableBuffer(commandList);

        final long currentFrameTime = ZonedDateTime.now().toInstant().toEpochMilli();

        final float fadeCoeffChange = lastFrameTime == 0L ? 0
                : (currentFrameTime - lastFrameTime) * Config.fadeCoeffPerMs;

        for (RenderSection chunk : chunks)
            processChunk(fadeCoeffChange, chunk);

        chunkFadeCoeffsBuffer.uploadData(commandList, chunkGlFadeCoeffBuffer);
        shader.setFadeCoeffs(chunkGlFadeCoeffBuffer);
        lastFrameTime = currentFrameTime;
    }

    public void delete(CommandList commandList) {
        chunkFadeCoeffsBuffer.delete();

        if (chunkGlFadeCoeffBuffer != null)
            commandList.deleteBuffer(chunkGlFadeCoeffBuffer);
    }

    private void checkMutableBuffer(CommandList commandList) {
        if (chunkGlFadeCoeffBuffer == null)
            chunkGlFadeCoeffBuffer = commandList.createMutableBuffer();
    }

    private void processChunk(final float fadeCoeffChange, RenderSection chunk) {
        final int chunkId = chunk.getChunkId();

        float fadeCoeff = chunkFadeCoeffsBuffer.get(chunkId, 3);

        fadeCoeff += fadeCoeffChange;

        if (fadeCoeff == 1f)
            return;

        if (fadeCoeff > 1f)
            fadeCoeff = 1f;

        chunkFadeCoeffsBuffer.put(chunkId, 3, fadeCoeff);
    }
}
