package com.koteinik.chunksfadein.mixin.entity;

import org.spongepowered.asm.mixin.Mixin;

import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.LastRenderOffsetStorage;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.Vec3d;

@Mixin(value = BlockEntity.class)
public class BlockEntityMixin implements LastRenderOffsetStorage {
    private Vec3d lastRenderOffset = new Vec3d(0, -Config.animationInitialOffset, 0);

    @Override
    public Vec3d getLastRenderOffset() {
        return lastRenderOffset;
    }

    @Override
    public void setLastRenderOffset(Vec3d vec) {
        lastRenderOffset = vec;
    }
}
