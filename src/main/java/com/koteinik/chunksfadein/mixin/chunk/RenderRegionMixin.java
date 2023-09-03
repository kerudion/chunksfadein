package com.koteinik.chunksfadein.mixin.chunk;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.ChunkFadeInController;
import com.koteinik.chunksfadein.extensions.ChunkShaderInterfaceExt;
import com.koteinik.chunksfadein.extensions.RenderRegionExt;
import com.koteinik.chunksfadein.extensions.RenderSectionExt;

import me.jellysquid.mods.sodium.client.gl.device.CommandList;
import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegion;
import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegionManager;
import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegion.RenderRegionArenas;

@Mixin(value = RenderRegion.class, remap = false)
public class RenderRegionMixin implements RenderRegionExt {
    private ChunkFadeInController fadeController;

    @Inject(method = "<init>", at = @At(value = "TAIL"))
    private void modifyConstructor(RenderRegionManager manager, int x, int y, int z, CallbackInfo ci) {
        if (!Config.isModEnabled)
            return;

        fadeController = new ChunkFadeInController();
    }

    @Inject(method = "getOrCreateArenas", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/chunk/region/RenderRegionManager;createRegionArenas"), cancellable = true)
    private void modifyGetOrCreateArenas(CommandList commandList, CallbackInfoReturnable<RenderRegionArenas> cir) {
        if (!Config.isModEnabled)
            return;

        fadeController.createBuffer(commandList);
    }

    @Inject(method = "deleteResources", at = @At(value = "TAIL"))
    private void modifyDeleteResources(CommandList commandList, CallbackInfo ci) {
        if (!Config.isModEnabled)
            return;

        fadeController.delete(commandList);
    }

    @Override
    public void processChunk(RenderSectionExt chunk, int chunkId, int x, int y, int z) {
        fadeController.processChunk(chunk, chunkId, x, y, z);
    }

    @Override
    public void uploadToBuffer(ChunkShaderInterfaceExt shader, CommandList commandList) {
        fadeController.uploadToBuffer(shader, commandList);
    }
}
