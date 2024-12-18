package com.koteinik.chunksfadein.mixin.chunk;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.koteinik.chunksfadein.core.ChunkFadeInController;
import com.koteinik.chunksfadein.extensions.ChunkShaderInterfaceExt;
import com.koteinik.chunksfadein.extensions.RenderRegionExt;
import com.koteinik.chunksfadein.extensions.RenderSectionExt;

import net.caffeinemc.mods.sodium.client.gl.arena.staging.StagingBuffer;
import net.caffeinemc.mods.sodium.client.gl.device.CommandList;
import net.caffeinemc.mods.sodium.client.gl.device.GLRenderDevice;
import net.caffeinemc.mods.sodium.client.render.chunk.region.RenderRegion;

@Mixin(value = RenderRegion.class, remap = false)
public class RenderRegionMixin implements RenderRegionExt {
    private ChunkFadeInController fadeController;

    @Inject(method = "<init>", at = @At(value = "TAIL"))
    private void modifyConstructor(int x, int y, int z, StagingBuffer stagingBuffer, CallbackInfo ci) {
        fadeController = new ChunkFadeInController(GLRenderDevice.INSTANCE.createCommandList());
    }

    @Inject(method = "delete", at = @At(value = "TAIL"))
    private void modifyDeleteResources(CommandList commandList, CallbackInfo ci) {
        fadeController.delete(commandList);
    }

    @Override
    public void processChunk(RenderSectionExt section, int sectionIndex) {
        fadeController.processChunk(section, sectionIndex);
    }

    @Override
    public void uploadToBuffer(ChunkShaderInterfaceExt shader, CommandList commandList) {
        fadeController.uploadToBuffer(shader, commandList);
    }
}