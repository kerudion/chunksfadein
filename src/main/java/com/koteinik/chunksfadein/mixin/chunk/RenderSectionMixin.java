package com.koteinik.chunksfadein.mixin.chunk;

import org.spongepowered.asm.mixin.Mixin;

import com.koteinik.chunksfadein.extenstions.RenderSectionExt;

import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;

@Mixin(value = RenderSection.class)
public class RenderSectionMixin implements RenderSectionExt {
    private boolean hasRenderedBefore;

    @Override
    public boolean hasRenderedBefore() {
        return hasRenderedBefore;
    }

    @Override
    public void setRenderedBefore() {
        hasRenderedBefore = true;
    }
}
