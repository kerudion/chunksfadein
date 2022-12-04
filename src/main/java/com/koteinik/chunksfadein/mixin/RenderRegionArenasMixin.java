package com.koteinik.chunksfadein.mixin;

import java.nio.ByteBuffer;
import java.util.List;

import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.koteinik.chunksfadein.extenstions.ChunkShaderInterfaceExt;
import com.koteinik.chunksfadein.extenstions.RenderRegionArenasExt;

import me.jellysquid.mods.sodium.client.gl.arena.staging.StagingBuffer;
import me.jellysquid.mods.sodium.client.gl.buffer.GlBufferUsage;
import me.jellysquid.mods.sodium.client.gl.buffer.GlMutableBuffer;
import me.jellysquid.mods.sodium.client.gl.device.CommandList;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegion;
import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegion.RenderRegionArenas;

@Mixin(value = RenderRegionArenas.class, remap = false)
public class RenderRegionArenasMixin implements RenderRegionArenasExt {
    private static final int fadeCoeffStride = 4 * 4;
    private final ByteBuffer chunkFadeCoeffsBuffer = createFadeCoeffsBuffer();
    private GlMutableBuffer chunkGlFadeCoeffBuffer;
    private CommandList commandList;

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    private void modifyConstructor(CommandList commandList, StagingBuffer stagingBuffer, CallbackInfo ci) {
        chunkGlFadeCoeffBuffer = commandList.createMutableBuffer();
        this.commandList = commandList;
        uploadFadeCoeffDataToGl();
    }

    @Inject(method = "delete", at = @At(value = "TAIL"))
    private void modifyDelete(CommandList commandList, CallbackInfo ci) {
        chunkFadeCoeffsBuffer.clear();
        commandList.deleteBuffer(chunkGlFadeCoeffBuffer);
    }

    public void updateChunksFade(List<RenderSection> chunks, ChunkShaderInterfaceExt shader) {
        for (RenderSection chunk : chunks) {
            final int chunkId = chunk.getChunkId();
            float fadeCoeff = chunkFadeCoeffsBuffer.getFloat(chunkId * fadeCoeffStride);
            fadeCoeff += fadeCoeff < 1f ? 0.025f : 0f;

            chunkFadeCoeffsBuffer.putFloat(chunkId * fadeCoeffStride, fadeCoeff);
        }

        uploadFadeCoeffDataToGl();
        shader.setFadeCoeffs(chunkGlFadeCoeffBuffer);
    }

    private void uploadFadeCoeffDataToGl() {
        commandList.uploadData(chunkGlFadeCoeffBuffer, chunkFadeCoeffsBuffer, GlBufferUsage.DYNAMIC_DRAW);
    }

    private ByteBuffer createFadeCoeffsBuffer() {
        ByteBuffer data = MemoryUtil.memAlloc(RenderRegion.REGION_SIZE * fadeCoeffStride);

        for (int i = 0; i < RenderRegion.REGION_SIZE; i++)
            data.putFloat(i * fadeCoeffStride, 0f);

        return data;
    }
}
