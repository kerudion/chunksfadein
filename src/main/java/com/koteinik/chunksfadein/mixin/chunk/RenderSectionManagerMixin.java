package com.koteinik.chunksfadein.mixin.chunk;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.extensions.RenderSectionExt;
import com.koteinik.chunksfadein.extensions.RenderSectionManagerExt;

import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;

@Mixin(value = RenderSectionManager.class, remap = false)
public abstract class RenderSectionManagerMixin implements RenderSectionManagerExt {
    @Override
    public float[] getAnimationOffset(int x, int y, int z) {
        RenderSection section = getRenderSection(x, y, z);
        if (section == null)
            return null;

        return ((RenderSectionExt) section).getAnimationOffset();
    }

    @Override
    public float getFadeCoeff(int x, int y, int z) {
        RenderSection section = getRenderSection(x, y, z);
        if (section == null)
            return 0f;

        RenderSectionExt ext = (RenderSectionExt) section;
        if (ext.hasRenderedBefore() && !Config.isFadeEnabled)
            return 1f;

        return ext.getFadeCoeff();
    }

    @Shadow
    private RenderSection getRenderSection(int x, int y, int z) {
        return null;
    }
}
