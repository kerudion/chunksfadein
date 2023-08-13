package com.koteinik.chunksfadein.mixin.chunk;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

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
        if (y > 19 || y < -4)
            return 1f;

        RenderSection section = getRenderSection(x, y, z);
        if (section == null)
            return 0f;
        if (section.isBuilt() && section.isEmpty())
            return 1f;

        return ((RenderSectionExt) section).getFadeCoeff();
    }

    @Shadow
    private RenderSection getRenderSection(int x, int y, int z) {
        return null;
    }
}
