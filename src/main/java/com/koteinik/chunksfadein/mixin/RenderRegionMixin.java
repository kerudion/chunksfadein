package com.koteinik.chunksfadein.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.koteinik.chunksfadein.extenstions.RenderRegionArenasExt;

import org.spongepowered.asm.mixin.injection.At;

import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegion;
import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegion.RenderRegionArenas;

@Mixin(value = RenderRegion.class, remap = false)
public class RenderRegionMixin {
    @Shadow(remap = false)
    private RenderRegionArenas arenas;

    @Inject(method = "removeChunk", at = @At(value = "TAIL"))
    private void modifyRemoveChunk(RenderSection chunk, CallbackInfo ci) {
        if (arenas == null)
            return;
        RenderRegionArenasExt arenasExt = (RenderRegionArenasExt) arenas;
        arenasExt.resetFadeCoeffForChunk(chunk);
    }
}
