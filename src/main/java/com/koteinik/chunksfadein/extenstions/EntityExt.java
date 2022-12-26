package com.koteinik.chunksfadein.extenstions;

import net.minecraft.util.math.Vec3d;

public interface EntityExt {
    public Vec3d getLastRenderOffset();
    public void setLastRenderOffset(Vec3d vec);
}
