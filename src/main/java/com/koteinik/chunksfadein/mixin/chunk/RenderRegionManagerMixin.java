package com.koteinik.chunksfadein.mixin.chunk;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.ChunkAppearedLink;
import com.koteinik.chunksfadein.extenstions.RenderRegionManagerExt;

import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;
import me.jellysquid.mods.sodium.client.gl.device.CommandList;
import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegion;
import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegionManager;

@Mixin(value = RenderRegionManager.class, remap = false)
public class RenderRegionManagerMixin implements RenderRegionManagerExt {
    @Shadow
    private final Long2ReferenceOpenHashMap<RenderRegion> regions = new Long2ReferenceOpenHashMap<RenderRegion>();

    @Inject(method = "<init>", at = @At(value = "TAIL"))
    private void modifyConstructor(CommandList commandList, CallbackInfo ci) {
        if (!Config.isModEnabled)
            return;

        ChunkAppearedLink.regionManager = this;
    }

    @Override
    public RenderRegion getRenderRegion(int chunkX, int chunkY, int chunkZ) {
        long key = RenderRegion.getRegionKeyForChunk(chunkX, chunkY, chunkZ);
        return regions.get(key);
    }
}
