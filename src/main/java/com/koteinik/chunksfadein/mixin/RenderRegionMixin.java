package com.koteinik.chunksfadein.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.koteinik.chunksfadein.Config;
import com.koteinik.chunksfadein.core.ChunkFadeInController;
import com.koteinik.chunksfadein.extenstions.ChunkShaderInterfaceExt;
import com.koteinik.chunksfadein.extenstions.RenderRegionExt;

import org.spongepowered.asm.mixin.injection.At;

import me.jellysquid.mods.sodium.client.gl.device.CommandList;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegion;
import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegionManager;

@Mixin(value = RenderRegion.class, remap = false)
public class RenderRegionMixin implements RenderRegionExt {
    private final boolean needToDisable = Config.needToTurnOff();
    private ChunkFadeInController fadeController;

    @Inject(method = "<init>", at = @At(value = "TAIL"))
    private void modifyConstructor(RenderRegionManager manager, int x, int y, int z, CallbackInfo ci) {
        if (needToDisable)
            return;

        fadeController = new ChunkFadeInController();
    }

    @Inject(method = "removeChunk", at = @At(value = "TAIL"))
    private void modifyRemoveChunk(RenderSection chunk, CallbackInfo ci) {
        if (needToDisable)
            return;

        fadeController.resetFadeCoeffForChunk(chunk);
    }

    @Inject(method = "deleteResources", at = @At(value = "TAIL"))
    private void modifyDeleteResources(CommandList commandList, CallbackInfo ci) {
        if (needToDisable)
            return;

        fadeController.delete(commandList);
    }

    @Override
    public void updateChunksFade(List<RenderSection> chunks, ChunkShaderInterfaceExt shader, CommandList commandList) {
        fadeController.updateChunksFade(chunks, shader, commandList);
    }
}
