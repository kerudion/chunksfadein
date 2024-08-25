package com.koteinik.chunksfadein.extensions;

import net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager;
import net.minecraft.util.math.Vec3d;

import org.jetbrains.annotations.Nullable;

public interface SodiumWorldRendererExt {
    float[] getAnimationOffset(Vec3d pos);
    @Nullable
    RenderSectionManager getRenderSectionManager();
}
